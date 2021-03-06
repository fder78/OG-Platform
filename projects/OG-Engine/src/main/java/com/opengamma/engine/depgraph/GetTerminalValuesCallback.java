/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.target.digest.TargetDigests;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/**
 * Handles callback notifications of terminal values to populate a graph set.
 */
/* package */class GetTerminalValuesCallback implements ResolvedValueCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(GetTerminalValuesCallback.class);

  private static final int MAX_DATA_PER_DIGEST = 4;

  private static class PerFunctionNodeInfo {

    /**
     * All nodes using this function, indexed by target specification.
     */
    private final Map<ComputationTargetSpecification, Set<DependencyNode>> _target2nodes = new HashMap<ComputationTargetSpecification, Set<DependencyNode>>();

    /**
     * All targets that a {@link ComputationTargetCollapser} can apply to, with its checking group. Any targets in the same group have already been mutually compared, and cannot be collapsed.
     */
    private Map<ComputationTargetSpecification, Object> _target2collapseGroup;

    /**
     * All targets that a {@link ComputationTargetCollapser} can apply to, indexed by the checking group. Any targets in the same group have already been mutually compared, and cannot be collapsed.
     */
    private Map<Object, Collection<ComputationTargetSpecification>> _collapseGroup2targets;

    /**
     * Queue of targets that must be collapsed, the first element in each pair if the original target - the next element is the replacement target.
     */
    private Queue<Pair<ComputationTargetSpecification, ComputationTargetSpecification>> _collapse;

    public void storeForCollapse(final ComputationTargetSpecification targetSpecification) {
      if (_target2collapseGroup == null) {
        _target2collapseGroup = new HashMap<ComputationTargetSpecification, Object>();
        _collapseGroup2targets = new HashMap<Object, Collection<ComputationTargetSpecification>>();
        _collapse = new LinkedTransferQueue<Pair<ComputationTargetSpecification, ComputationTargetSpecification>>();
      }
      if (!_target2collapseGroup.containsKey(targetSpecification)) {
        final Object group = new Object();
        _target2collapseGroup.put(targetSpecification, group);
        _collapseGroup2targets.put(group, Collections.singleton(targetSpecification));
      }
    }

  }

  /**
   * Buffer of resolved value specifications. For any entries in here, all input values have been previously resolved and are in this buffer or the partially constructed graph. Information here gets
   * used to construct dependency graph fragments whenever a terminal item can be resolved.
   */
  private final ConcurrentMap<ValueSpecification, ResolvedValue> _resolvedBuffer = new ConcurrentHashMap<ValueSpecification, ResolvedValue>();

  /**
   * Index into the dependency graph nodes, keyed by their output specifications.
   */
  private final Map<ValueSpecification, DependencyNode> _spec2Node = new ConcurrentHashMap<ValueSpecification, DependencyNode>();

  /**
   * Index into the dependency graph nodes.
   */
  private final Map<ParameterizedFunction, PerFunctionNodeInfo> _func2nodeInfo = new HashMap<ParameterizedFunction, PerFunctionNodeInfo>();

  /**
   * All dependency graph nodes.
   */
  private final Set<DependencyNode> _graphNodes = new HashSet<DependencyNode>();

  /**
   * Terminal value resolutions, mapping the resolved value specifications back to the originally requested requested value requirements.
   */
  private final Map<ValueSpecification, Collection<ValueRequirement>> _resolvedValues = new HashMap<ValueSpecification, Collection<ValueRequirement>>();

  /**
   * Queue of completed resolutions that have not been processed into the partial graph. Graph construction is single threaded, with this queue holding work items if other thread produce results while
   * one is busy building the graph.
   */
  private final Queue<Pair<ValueRequirement, ResolvedValue>> _resolvedQueue = new LinkedTransferQueue<Pair<ValueRequirement, ResolvedValue>>();

  /**
   * Index of functions which have an active collapse set.
   */
  private final Set<ParameterizedFunction> _collapsers = new HashSet<ParameterizedFunction>();

  /**
   * Mutex for working on the resolved queue. Any thread can add to the queue, only the one that has claimed this mutex can process the elements from it and be sure of exclusive access to the other
   * data structures.
   */
  private final AtomicReference<Thread> _singleton = new AtomicReference<Thread>();

  /**
   * Optional listener to process failures.
   */
  private ResolutionFailureListener _failureListener;

  /**
   * Optional logic to collapse nodes on mutually compatible targets into a single node.
   */
  private ComputationTargetCollapser _computationTargetCollapser;

  /**
   * Optional logic to produce candidate matches based on previous resolutions of similar targets.
   */
  private TargetDigests _targetDigests;

  /**
   * The current graph building context (the callback holds the write lock for the duration of other calls, so can set it here instead of passing it on the stack).
   */
  private GraphBuildingContext _context;

  private final Lock _readLock;
  private final Lock _writeLock;

  /**
   * The target digest map which may be used to speed up selection choices instead of full back-tracking.
   * <p>
   * The pair elements either contains the values, or arrays of the values. The first is either ValueProperties (or an array of them). The second is either ParameterizedFunction (or an array of them).
   */
  private final ConcurrentMap<Object, ConcurrentMap<String, Pair<?, ?>>> _targetDigestInfo = new ConcurrentHashMap<Object, ConcurrentMap<String, Pair<?, ?>>>();

  public GetTerminalValuesCallback(final ResolutionFailureListener failureListener) {
    _failureListener = failureListener;
    final ReadWriteLock rwl = new ReentrantReadWriteLock();
    _readLock = rwl.readLock();
    _writeLock = rwl.writeLock();
  }

  public void setFailureListener(ResolutionFailureListener failureListener) {
    _failureListener = failureListener;
  }

  public void setComputationTargetCollapser(final ComputationTargetCollapser collapser) {
    _computationTargetCollapser = collapser;
  }

  public void setTargetDigests(final TargetDigests targetDigests) {
    _targetDigests = targetDigests;
  }

  public ResolvedValue getProduction(final ValueSpecification specification) {
    final ResolvedValue value = _resolvedBuffer.get(specification);
    if (value != null) {
      return value;
    }
    final DependencyNode node = _spec2Node.get(specification);
    if (node == null) {
      return null;
    }
    // Can only use the specification if it is consumed by another node (or is a terminal output); i.e. it has been fully resolved
    // and is not just an advisory used to merge tentative results to give a single node producing multiple outputs.
    _readLock.lock();
    try {
      if (node.hasTerminalOutputValue(specification)) {
        return new ResolvedValue(specification, node.getFunction(), node.getInputValuesCopy(), node.getOutputValuesCopy());
      }
      for (final DependencyNode dependent : node.getDependentNodes()) {
        if (dependent.hasInputValue(specification)) {
          return new ResolvedValue(specification, node.getFunction(), node.getInputValuesCopy(), node.getOutputValuesCopy());
        }
      }
    } finally {
      _readLock.unlock();
    }
    return null;
  }

  private void storeResolution(final Object targetDigest, final ValueSpecification resolvedValue, final ParameterizedFunction function) {
    ConcurrentMap<String, Pair<?, ?>> info = _targetDigestInfo.get(targetDigest);
    if (info == null) {
      info = new ConcurrentHashMap<String, Pair<?, ?>>();
      info.put(resolvedValue.getValueName(), Pair.of(resolvedValue.getProperties(), function));
      final ConcurrentMap<String, Pair<?, ?>> existing = _targetDigestInfo.putIfAbsent(targetDigest, info);
      if (existing == null) {
        return;
      }
      info = existing;
    }
    final ValueProperties resolvedProperties = resolvedValue.getProperties();
    Pair<?, ?> oldValues = info.get(resolvedValue.getValueName());
    Pair<?, ?> newValues;
    do {
      if (oldValues == null) {
        newValues = Pair.of(resolvedProperties, function);
        oldValues = info.putIfAbsent(resolvedValue.getValueName(), newValues);
        if (oldValues == null) {
          return;
        }
      } else {
        final Object propertiesObj = oldValues.getFirst();
        final ParameterizedFunction[] newFunctions;
        final ValueProperties[] newProperties;
        if (propertiesObj instanceof ValueProperties) {
          if (resolvedProperties.equals(propertiesObj)) {
            return;
          }
          newProperties = new ValueProperties[2];
          newProperties[0] = resolvedProperties;
          newProperties[1] = (ValueProperties) propertiesObj;
          newFunctions = new ParameterizedFunction[2];
          newFunctions[0] = function;
          newFunctions[1] = (ParameterizedFunction) oldValues.getSecond();
        } else {
          // For small lengths, this is cheaper than set operations
          final ValueProperties[] oldProperties = (ValueProperties[]) propertiesObj;
          for (ValueProperties properties : oldProperties) {
            // ValueProperties that are part of the ValueSpecifications that go into ResolvedValue are in a normalized form so we can do a cheap comparison
            if (resolvedProperties == properties) {
              return;
            }
            assert !properties.equals(resolvedProperties);
          }
          if (oldProperties.length >= MAX_DATA_PER_DIGEST) {
            newProperties = new ValueProperties[MAX_DATA_PER_DIGEST];
            newProperties[0] = resolvedProperties;
            System.arraycopy(oldProperties, 0, newProperties, 1, MAX_DATA_PER_DIGEST - 1);
            newFunctions = new ParameterizedFunction[MAX_DATA_PER_DIGEST];
            newFunctions[0] = function;
            System.arraycopy(oldValues.getSecond(), 0, newFunctions, 1, MAX_DATA_PER_DIGEST - 1);
          } else {
            newProperties = new ValueProperties[oldProperties.length + 1];
            newProperties[0] = resolvedProperties;
            System.arraycopy(oldProperties, 0, newProperties, 1, oldProperties.length);
            newFunctions = new ParameterizedFunction[oldProperties.length + 1];
            newFunctions[0] = function;
            System.arraycopy(oldValues.getSecond(), 0, newFunctions, 1, oldProperties.length);
          }
        }
        if (info.replace(resolvedValue.getValueName(), oldValues, Pair.of(newProperties, newFunctions))) {
          return;
        }
        oldValues = info.get(resolvedValue.getValueName());
      }
    } while (true);
  }

  public Pair<?, ?> getResolutions(final FunctionCompilationContext context, final ComputationTargetSpecification targetSpec, final String valueName) {
    if (_targetDigests == null) {
      return null;
    }
    final Object targetDigest = _targetDigests.getDigest(context, targetSpec);
    if (targetDigest == null) {
      return null;
    }
    final Map<String, Pair<?, ?>> info = _targetDigestInfo.get(targetDigest);
    if (info != null) {
      return info.get(valueName);
    } else {
      return null;
    }
  }

  public void declareProduction(final ResolvedValue resolvedValue) {
    _resolvedBuffer.put(resolvedValue.getValueSpecification(), resolvedValue);
  }

  @Override
  public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
    s_logger.info("Couldn't resolve {}", value);
    if (failure != null) {
      final ResolutionFailure failureImpl = failure.checkFailure(value);
      if (_failureListener != null) {
        _failureListener.notifyFailure(failureImpl);
      }
      context.exception(new UnsatisfiableDependencyGraphException(failureImpl));
    } else {
      s_logger.warn("No failure state for {}", value);
      context.exception(new UnsatisfiableDependencyGraphException(value));
    }
  }

  public void populateState(final DependencyGraph graph, final FunctionCompilationContext context) {
    _writeLock.lock();
    try {
      final Set<DependencyNode> remove = new HashSet<DependencyNode>();
      for (final DependencyNode node : graph.getDependencyNodes()) {
        for (final DependencyNode dependent : node.getDependentNodes()) {
          if (!graph.containsNode(dependent)) {
            // Need to remove "dependent" from the node. We can leave the output values there; they might be used, or will get discarded by discardUnusedOutputs afterwards
            remove.add(dependent);
          }
        }
        _graphNodes.add(node);
        final Object targetDigest;
        if (_targetDigests != null) {
          targetDigest = _targetDigests.getDigest(context, node.getComputationTarget());
        } else {
          targetDigest = null;
        }
        for (final ValueSpecification output : node.getOutputValues()) {
          _spec2Node.put(output, node);
          if (targetDigest != null) {
            storeResolution(targetDigest, output, node.getFunction());
          }
        }
        getOrCreateNodes(node.getFunction(), node.getComputationTarget()).add(node);
      }
      for (final DependencyNode node : remove) {
        node.clearInputs();
      }
      for (final Map.Entry<ValueSpecification, Set<ValueRequirement>> terminal : graph.getTerminalOutputs().entrySet()) {
        _resolvedValues.put(terminal.getKey(), new ArrayList<ValueRequirement>(terminal.getValue()));
      }
    } finally {
      _writeLock.unlock();
    }
  }

  private class CollapseNodes implements ContextRunnable {

    private final ParameterizedFunction _function;
    private final PerFunctionNodeInfo _nodeInfo;
    private final ComputationTargetSpecification[] _a;
    private final ComputationTargetSpecification[] _b;

    public CollapseNodes(ParameterizedFunction function, final PerFunctionNodeInfo nodeInfo, final Collection<ComputationTargetSpecification> a, final Collection<ComputationTargetSpecification> b) {
      _function = function;
      _nodeInfo = nodeInfo;
      _a = a.toArray(new ComputationTargetSpecification[a.size()]);
      _b = b.toArray(new ComputationTargetSpecification[b.size()]);
    }

    // ContextRunnable

    @Override
    public boolean tryRun(final GraphBuildingContext context) {
      int aLength = _a.length;
      int bLength = _b.length;
      for (int i = 0; i < aLength; i++) {
        final ComputationTargetSpecification a = _a[i];
        for (int j = 0; j < bLength; j++) {
          final ComputationTargetSpecification b = _b[j];
          assert !a.equals(b);
          final ComputationTargetSpecification collapsed = _computationTargetCollapser.collapse(_function.getFunction(), a, b);
          if (collapsed != null) {
            if (collapsed.equals(a)) {
              // A and B merged into A
              _b[j--] = _b[--bLength];
              _nodeInfo._collapse.add(Pair.of(b, a));
              s_logger.debug("Merging {} into {}", b, a);
            } else if (collapsed.equals(b)) {
              // A and B merged into B
              _a[i--] = _a[--aLength];
              _nodeInfo._collapse.add(Pair.of(a, b));
              s_logger.debug("Merging {} into {}", a, b);
              break;
            } else {
              // A and B merged into new target
              _a[i--] = _a[--aLength];
              _b[j] = _b[--bLength];
              // Note the new target will go into its own evaluation group when this is actioned; it will then be compared against the other targets
              _nodeInfo._collapse.add(Pair.of(a, collapsed));
              _nodeInfo._collapse.add(Pair.of(b, collapsed));
              if (s_logger.isDebugEnabled()) {
                s_logger.debug("Merging {} and {} into new node {}", new Object[] {a, b, collapsed });
              }
              break;
            }
          }
        }
      }
      Collection<ComputationTargetSpecification> targets = new ArrayList<ComputationTargetSpecification>(aLength + bLength);
      for (int i = 0; i < aLength; i++) {
        targets.add(_a[i]);
      }
      for (int i = 0; i < bLength; i++) {
        targets.add(_b[i]);
      }
      final Object group = new Object();
      // TODO: Waiting for the lock could be costly; we could post this to a queue like the resolved values do
      _writeLock.lock();
      try {
        for (int i = 0; i < aLength; i++) {
          _nodeInfo._target2collapseGroup.put(_a[i], group);
        }
        for (int i = 0; i < bLength; i++) {
          _nodeInfo._target2collapseGroup.put(_b[i], group);
        }
        _nodeInfo._collapseGroup2targets.put(group, targets);
        _context = context;
        scheduleCollapsers(_function, _nodeInfo);
      } finally {
        _writeLock.unlock();
      }
      return true;
    }
  }

  // TODO: Multiple nodes for a single collapse applicable target should be collapsed, probably with a "collapse(function, a, a)" sanity check first

  // Note: we're not adjusting the target digests during collapses; for the collapse to have made sense, the digests for each target should probably be the same.
  // TODO: This might be a bad assumption

  private void scheduleCollapsers(final ParameterizedFunction function, final PerFunctionNodeInfo nodeInfo) {
    // Action anything already found asynchronously
    Pair<ComputationTargetSpecification, ComputationTargetSpecification> collapse = nodeInfo._collapse.poll();
    while (collapse != null) {
      s_logger.debug("Found collapse targets {}", collapse);
      nodeInfo._target2collapseGroup.remove(collapse.getFirst());
      final Set<DependencyNode> originalNodes = nodeInfo._target2nodes.remove(collapse.getFirst());
      final Set<DependencyNode> newNodes = getOrCreateNodes(function, collapse.getSecond());
      final DependencyNode newNode;
      if (newNodes.isEmpty()) {
        newNode = new DependencyNode(collapse.getSecond());
        newNode.setFunction(function);
        newNodes.add(newNode);
        _graphNodes.add(newNode);
      } else {
        // TODO: See comment above about whether multiple nodes for a single collapse applicable target should exist
        newNode = newNodes.iterator().next();
      }
      for (DependencyNode originalNode : originalNodes) {
        s_logger.debug("Applying collapse of {} into {}", originalNode, newNode);
        if (!_graphNodes.remove(originalNode)) {
          s_logger.error("Assertion error - {} is not in the graph", originalNode);
          continue;
        }
        originalNode.replaceWith(newNode);
        for (final ValueSpecification output : originalNode.getOutputValues()) {
          final ValueSpecification newOutput = MemoryUtils.instance(new ValueSpecification(output.getValueName(), newNode.getComputationTarget(), output.getProperties()));
          _spec2Node.put(output, newNode);
          _spec2Node.put(newOutput, newNode);
          // TODO: Should update the target digest data
          final Collection<ValueRequirement> requirements = _resolvedValues.remove(output);
          if (requirements != null) {
            _resolvedValues.put(newOutput, requirements);
          }
        }
      }
      collapse = nodeInfo._collapse.poll();
    }
    // Schedule collapsing tasks to run asynchronously
    int collapseGroups = nodeInfo._collapseGroup2targets.size();
    if (collapseGroups > 1) {
      final Iterator<Map.Entry<ComputationTargetSpecification, Object>> itrTarget2CollapseGroup = nodeInfo._target2collapseGroup.entrySet().iterator();
      do {
        final Map.Entry<ComputationTargetSpecification, Object> target2collapseGroup = itrTarget2CollapseGroup.next();
        if (!nodeInfo._target2nodes.containsKey(target2collapseGroup.getKey())) {
          // Note: This happens because entries get written into the nodeInfo as soon as a target is requested. The target might not result in any
          // nodes being created because of an earlier substitution. This is a simple solution - an alternative and possible faster method is to not
          // create the target2collapseGroup entry until a node is created. The alternative is harder to implement though! 
          s_logger.debug("Found transient key {}", target2collapseGroup);
          final Collection<ComputationTargetSpecification> targetSpecs = nodeInfo._collapseGroup2targets.get(target2collapseGroup.getValue());
          if (targetSpecs.size() == 1) {
            if (targetSpecs.contains(target2collapseGroup.getKey())) {
              nodeInfo._collapseGroup2targets.remove(target2collapseGroup.getValue());
              collapseGroups--;
            } else {
              s_logger.error("Assertion error - transient singleton key {} not in reverse lookup table", target2collapseGroup.getKey());
            }
          } else {
            if (!targetSpecs.remove(target2collapseGroup.getKey())) {
              s_logger.error("Assertion error - transient key {} not in reverse lookup table", target2collapseGroup.getKey());
            }
          }
          itrTarget2CollapseGroup.remove();
        }
      } while (itrTarget2CollapseGroup.hasNext());
      if (collapseGroups > 1) {
        final Iterator<Collection<ComputationTargetSpecification>> itrCollapseGroup2Targets = nodeInfo._collapseGroup2targets.values().iterator();
        do {
          final Collection<ComputationTargetSpecification> a = itrCollapseGroup2Targets.next();
          if (!itrCollapseGroup2Targets.hasNext()) {
            break;
          }
          itrCollapseGroup2Targets.remove();
          final Collection<ComputationTargetSpecification> b = itrCollapseGroup2Targets.next();
          itrCollapseGroup2Targets.remove();
          _context.submit(new CollapseNodes(function, nodeInfo, a, b));
        } while (itrCollapseGroup2Targets.hasNext());
      }
    }
  }

  private void scheduleCollapsers() {
    if (!_collapsers.isEmpty()) {
      final Iterator<ParameterizedFunction> itrCollapsers = _collapsers.iterator();
      do {
        final ParameterizedFunction function = itrCollapsers.next();
        final PerFunctionNodeInfo nodeInfo = _func2nodeInfo.get(function);
        scheduleCollapsers(function, nodeInfo);
      } while (itrCollapsers.hasNext());
    }
  }

  private DependencyNode getOrCreateNode(final ResolvedValue resolvedValue, final Set<ValueSpecification> downstream, DependencyNode node,
      final boolean newNode) {
    Set<ValueSpecification> downstreamCopy = null;
    for (final ValueSpecification input : resolvedValue.getFunctionInputs()) {
      DependencyNode inputNode;
      inputNode = _spec2Node.get(input);
      if (inputNode != null) {
        s_logger.debug("Found node {} for input {}", inputNode, input);
        if (input.getTargetSpecification().equals(inputNode.getComputationTarget())) {
          node.addInputValue(input);
        } else {
          // The node we connected to is a substitute following a target collapse; the original input value is now incorrect
          final ValueSpecification substituteInput = MemoryUtils.instance(new ValueSpecification(input.getValueName(), inputNode.getComputationTarget(), input.getProperties()));
          assert inputNode.getOutputValues().contains(substituteInput);
          node.addInputValue(substituteInput);
        }
        node.addInputNode(inputNode);
      } else {
        s_logger.debug("Finding node production for {}", input);
        final ResolvedValue inputValue = _resolvedBuffer.get(input);
        if (inputValue != null) {
          if (downstreamCopy == null) {
            downstreamCopy = new HashSet<ValueSpecification>(downstream);
            downstreamCopy.add(resolvedValue.getValueSpecification());
            s_logger.debug("Downstream = {}", downstreamCopy);
          }
          inputNode = getOrCreateNode(inputValue, downstreamCopy);
          if (inputNode != null) {
            node.addInputNode(inputNode);
            node.addInputValue(input);
          } else {
            s_logger.warn("No node production for {}", inputValue);
            return null;
          }
        } else {
          s_logger.warn("No registered production for {}", input);
          return null;
        }
      }
    }
    if (newNode) {
      s_logger.debug("Adding {} to graph set", node);
      // [PLAT-346] Here is a good spot to tackle PLAT-346; which node's outputs do we discard if there are multiple
      // productions for a given value specification?
      for (final ValueSpecification valueSpecification : resolvedValue.getFunctionOutputs()) {
        final DependencyNode existing = _spec2Node.get(valueSpecification);
        if (existing == null) {
          _spec2Node.put(valueSpecification, node);
        } else {
          // Simplest to keep the existing one (otherwise have to reconnect dependent nodes in the graph)
          node.removeOutputValue(valueSpecification);
        }
      }
      _graphNodes.add(node);
      final ValueSpecification valueSpecification = resolvedValue.getValueSpecification();
      _resolvedBuffer.remove(valueSpecification);
      if (_targetDigests != null) {
        final Object targetDigest = _targetDigests.getDigest(_context.getCompilationContext(), valueSpecification.getTargetSpecification());
        if (targetDigest != null) {
          storeResolution(targetDigest, valueSpecification, node.getFunction());
        }
      }
    }
    return node;
  }

  private DependencyNode getOrCreateNode(final ResolvedValue resolvedValue, final Set<ValueSpecification> downstream, final DependencyNode existingNode,
      final Set<DependencyNode> nodes) {
    if (existingNode != null) {
      return getOrCreateNode(resolvedValue, downstream, existingNode, false);
    } else {
      DependencyNode newNode = new DependencyNode(resolvedValue.getValueSpecification().getTargetSpecification());
      newNode.setFunction(resolvedValue.getFunction());
      newNode.addOutputValues(resolvedValue.getFunctionOutputs());
      newNode = getOrCreateNode(resolvedValue, downstream, newNode, true);
      if (newNode != null) {
        nodes.add(newNode);
      }
      return newNode;
    }
  }

  private Set<DependencyNode> getOrCreateNodes(final ParameterizedFunction function, final PerFunctionNodeInfo nodeInfo, final ComputationTargetSpecification targetSpecification) {
    Set<DependencyNode> nodes = nodeInfo._target2nodes.get(targetSpecification);
    if (nodes == null) {
      nodes = new HashSet<DependencyNode>();
      nodeInfo._target2nodes.put(targetSpecification, nodes);
      if ((_computationTargetCollapser != null) && _computationTargetCollapser.canApplyTo(targetSpecification)) {
        nodeInfo.storeForCollapse(targetSpecification);
        _collapsers.add(function);
      }
    }
    return nodes;
  }

  private Set<DependencyNode> getOrCreateNodes(final ParameterizedFunction function, final ComputationTargetSpecification targetSpecification) {
    PerFunctionNodeInfo nodeInfo = _func2nodeInfo.get(function);
    if (nodeInfo == null) {
      nodeInfo = new PerFunctionNodeInfo();
      _func2nodeInfo.put(function, nodeInfo);
    }
    return getOrCreateNodes(function, nodeInfo, targetSpecification);
  }

  private static boolean mismatchUnionImpl(final Set<ValueSpecification> as, final Set<ValueSpecification> bs) {
    nextA: for (final ValueSpecification a : as) { //CSIGNORE
      if (bs.contains(a)) {
        // Exact match
        continue;
      }
      final String aName = a.getValueName();
      final ValueProperties aProperties = a.getProperties();
      for (final ValueSpecification b : bs) {
        if (aName == b.getValueName()) {
          // Match the name; check the constraints
          if (aProperties.isSatisfiedBy(b.getProperties())) {
            continue nextA;
          } else {
            // Mismatch found
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Tests whether the union of value specifications would be mismatched; that is the two sets can't be composed. Given the intersection of common value names, the properties must be mutually
   * compatible.
   * 
   * @param as the first set of values, not null
   * @param bs the second set of values, not null
   * @return true if the values can't be composed, false if they can
   */
  private static boolean mismatchUnion(final Set<ValueSpecification> as, final Set<ValueSpecification> bs) {
    return mismatchUnionImpl(as, bs) || mismatchUnionImpl(bs, as);
  }

  private DependencyNode findExistingNode(final Set<DependencyNode> nodes, final ResolvedValue resolvedValue) {
    for (final DependencyNode node : nodes) {
      final Set<ValueSpecification> outputValues = node.getOutputValues();
      if (!mismatchUnion(outputValues, resolvedValue.getFunctionOutputs())) {
        s_logger.debug("Considering {} for {}", node, resolvedValue);
        // Update the output values for the node with the union. The input values will be dealt with by the caller.
        Map<ValueSpecification, ValueSpecification> replacements = null;
        resolvedOutput: for (final ValueSpecification output : resolvedValue.getFunctionOutputs()) { //CSIGNORE
          if (outputValues.contains(output)) {
            // Exact match found
            continue;
          }
          final String outputName = output.getValueName();
          final ValueProperties outputProperties = output.getProperties();
          for (final ValueSpecification outputValue : outputValues) {
            if (outputName == outputValue.getValueName()) {
              if ((replacements != null) && replacements.containsKey(outputValue)) {
                // The candidate output has already been re-written to match another of the resolved outputs
                continue;
              }
              if (outputValue.getProperties().isSatisfiedBy(outputProperties)) {
                // Found suitable match; check whether it needs rewriting
                final ValueProperties composedProperties = outputValue.getProperties().compose(outputProperties);
                if (!composedProperties.equals(outputValue.getProperties())) {
                  final ValueSpecification newOutputValue = MemoryUtils
                      .instance(new ValueSpecification(outputValue.getValueName(), outputValue.getTargetSpecification(), composedProperties));
                  if (replacements == null) {
                    replacements = Maps.newHashMapWithExpectedSize(outputValues.size());
                  }
                  replacements.put(outputValue, newOutputValue);
                }
                continue resolvedOutput;
              }
            }
          }
          // This output was not matched. The "mismatchUnion" test means it is in addition to what the node was previously producing
          // and should be able to produce once its got any extra inputs it needs.
          if (_spec2Node.containsKey(output)) {
            // Another node already produces this
            s_logger.debug("Discarding output {} - already produced elsewhere in the graph", output);
            // TODO: Would it be better to do this check at the start of the loop?
          } else {
            s_logger.debug("Adding additional output {} to {}", output, node);
            node.addOutputValue(output);
            _spec2Node.put(output, node);
          }
        }
        if (replacements != null) {
          for (Map.Entry<ValueSpecification, ValueSpecification> replacement : replacements.entrySet()) {
            final ValueSpecification oldValue = replacement.getKey();
            final ValueSpecification newValue = replacement.getValue();
            final int newConsumers = node.replaceOutputValue(oldValue, newValue);
            DependencyNode n = _spec2Node.remove(oldValue);
            assert n == node;
            n = _spec2Node.get(newValue);
            if (n != null) {
              // Reducing the value has created a collision ...
              if (newConsumers == 0) {
                // Keep the existing one (it's being used, or just an arbitrary choice if neither are used)
                node.removeOutputValue(newValue);
              } else {
                int existingConsumers = 0;
                for (final DependencyNode child : n.getDependentNodes()) {
                  if (child.hasInputValue(newValue)) {
                    existingConsumers++;
                  }
                }
                if (existingConsumers == 0) {
                  // Lose the existing (not being used), keep the new one
                  n.removeOutputValue(newValue);
                  _spec2Node.put(newValue, node);
                } else {
                  if (newConsumers <= existingConsumers) {
                    // Adjust the consumers of the reduced value to use the existing one
                    for (final DependencyNode child : node.getDependentNodes()) {
                      child.replaceInput(oldValue, newValue, node, n);
                    }
                    node.removeOutputValue(newValue);
                  } else {
                    // Adjust the consumers of the existing value to use the new one
                    for (final DependencyNode child : n.getDependentNodes()) {
                      child.replaceInput(oldValue, newValue, n, node);
                    }
                    n.removeOutputValue(newValue);
                    _spec2Node.put(newValue, node);
                  }
                }
              }
            } else {
              _spec2Node.put(newValue, node);
            }
            final Collection<ValueRequirement> oldRequirements = _resolvedValues.remove(oldValue);
            if (oldRequirements != null) {
              final Collection<ValueRequirement> newRequirements = _resolvedValues.get(newValue);
              if (newRequirements != null) {
                newRequirements.addAll(oldRequirements);
              } else {
                _resolvedValues.put(newValue, oldRequirements);
              }
            }
            // TODO: Should update the target digest data
          }
        }
        return node;
      }
    }
    return null;
  }

  private DependencyNode getOrCreateNode(final ResolvedValue resolvedValue, final Set<ValueSpecification> downstream) {
    s_logger.debug("Resolved {}", resolvedValue.getValueSpecification());
    if (downstream.contains(resolvedValue.getValueSpecification())) {
      s_logger.debug("Already have downstream production of {} in {}", resolvedValue.getValueSpecification(), downstream);
      return null;
    }
    final DependencyNode existingNode = _spec2Node.get(resolvedValue.getValueSpecification());
    if (existingNode != null) {
      s_logger.debug("Existing production of {} found in graph set", resolvedValue);
      return existingNode;
    }
    final Set<DependencyNode> nodes = getOrCreateNodes(resolvedValue.getFunction(), resolvedValue.getValueSpecification().getTargetSpecification());
    return getOrCreateNode(resolvedValue, downstream, findExistingNode(nodes, resolvedValue), nodes);
  }

  /**
   * Reports a successful resolution of a top level requirement. The production of linked {@link DependencyNode} instances to form the final graph is single threaded. The resolution is added to a
   * queue of successful resolutions. If this is the only (or first) thread to report resolutions then this will work to drain the queue and produce nodes for the graph based on the resolved value
   * cache in the building context. If other threads report resolutions while this is happening they are added to the queue and those threads return immediately.
   */
  @Override
  public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
    s_logger.info("Resolved {} to {}", valueRequirement, resolvedValue.getValueSpecification());
    if (pump != null) {
      context.close(pump);
    }
    _resolvedQueue.add(Pair.of(valueRequirement, resolvedValue));
    while (!_resolvedQueue.isEmpty() && _singleton.compareAndSet(null, Thread.currentThread())) {
      _writeLock.lock();
      try {
        _context = context;
        Pair<ValueRequirement, ResolvedValue> resolved = _resolvedQueue.poll();
        while (resolved != null) {
          final DependencyNode node = getOrCreateNode(resolved.getSecond(), Collections.<ValueSpecification>emptySet());
          if (node != null) {
            ValueSpecification outputValue = resolved.getSecond().getValueSpecification();
            if (!outputValue.getTargetSpecification().equals(node.getComputationTarget())) {
              outputValue = MemoryUtils.instance(new ValueSpecification(outputValue.getValueName(), node.getComputationTarget(), outputValue.getProperties()));
            }
            assert node.getOutputValues().contains(outputValue);
            node.addTerminalOutputValue(outputValue);
            Collection<ValueRequirement> requirements = _resolvedValues.get(outputValue);
            if (requirements == null) {
              requirements = new ArrayList<ValueRequirement>();
              _resolvedValues.put(outputValue, requirements);
            }
            requirements.add(resolved.getFirst());
          } else {
            s_logger.error("Resolved {} to {} but couldn't create one or more dependency node", resolved.getFirst(), resolved.getSecond().getValueSpecification());
          }
          resolved = _resolvedQueue.poll();
        }
        scheduleCollapsers();
      } finally {
        _writeLock.unlock();
      }
      _singleton.set(null);
    }
  }

  @Override
  public void recursionDetected() {
    // No-op
  }

  @Override
  public String toString() {
    return "TerminalValueCallback";
  }

  /**
   * Returns the dependency graph nodes built by calls to {@link #resolved}. It is only valid to call this when there are no pending resolutions - that is all calls to {@link #resolved} have returned.
   * A copy of the internal structure is used so that it may be modified by the caller and this callback instance be used to process subsequent resolutions.
   * 
   * @return the dependency graph nodes, not null
   */
  public Collection<DependencyNode> getGraphNodes() {
    _readLock.lock();
    try {
      return new ArrayList<DependencyNode>(_graphNodes);
    } finally {
      _readLock.unlock();
    }
  }

  /**
   * Returns the map of top level requirements requested of the graph builder to the specifications it produced that are in the dependency graph. Failed resolutions are not reported here. It is only
   * valid to call this when there are no pending resolutions - that is all calls to {@link #resolved} have returned. A copy of the internal structure is used so that it may be modified by the caller
   * and this callback instance be used to process subsequent resolutions.
   * 
   * @return the map of resolutions, not null
   */
  public Map<ValueRequirement, ValueSpecification> getTerminalValues() {
    _readLock.lock();
    try {
      final Map<ValueRequirement, ValueSpecification> result = Maps.newHashMapWithExpectedSize(_resolvedValues.size());
      for (final Map.Entry<ValueSpecification, Collection<ValueRequirement>> resolvedValues : _resolvedValues.entrySet()) {
        for (final ValueRequirement requirement : resolvedValues.getValue()) {
          result.put(requirement, resolvedValues.getKey());
        }
      }
      return result;
    } finally {
      _readLock.unlock();
    }
  }

  /**
   * Returns the inverse of the {@link #getTerminalValues} map, mapping the specifications to the value requirements that they satisfy.
   * 
   * @return the map of resolutions, not null
   */
  public Map<ValueSpecification, Set<ValueRequirement>> getTerminalValuesBySpecification() {
    _readLock.lock();
    try {
      final Map<ValueSpecification, Set<ValueRequirement>> result = Maps.newHashMapWithExpectedSize(_resolvedValues.size());
      for (final Map.Entry<ValueSpecification, Collection<ValueRequirement>> resolvedValues : _resolvedValues.entrySet()) {
        result.put(resolvedValues.getKey(), new HashSet<ValueRequirement>(resolvedValues.getValue()));
      }
      return result;
    } finally {
      _readLock.unlock();
    }
  }

  public void reportStateSize() {
    if (!s_logger.isInfoEnabled()) {
      return;
    }
    s_logger.info("Graph = {} nodes for {} terminal outputs", _graphNodes.size(), _resolvedValues.size());
    s_logger.info("Resolved buffer = {}, resolved queue = {}", _resolvedBuffer.size(), _resolvedQueue.size());
  }

}
