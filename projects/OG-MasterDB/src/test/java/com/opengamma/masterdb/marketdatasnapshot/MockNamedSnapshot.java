/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.marketdatasnapshot;

import com.opengamma.core.marketdatasnapshot.NamedSnapshot;
import com.opengamma.id.UniqueId;
import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableConstructor;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Named snapshot used to test database insertion/retrieval.
 */
@BeanDefinition
final class MockNamedSnapshot implements NamedSnapshot, ImmutableBean {

  @PropertyDefinition
  private final UniqueId _uniqueId;
  @PropertyDefinition
  private final String _name;
  @PropertyDefinition
  private final int _answer;

  @ImmutableConstructor
  MockNamedSnapshot(UniqueId uniqueId, String name, int answer) {
    _uniqueId = uniqueId;
    _name = name;
    _answer = answer;
  }

  @Override
  public NamedSnapshot withUniqueId(UniqueId uniqueId) {
    return new MockNamedSnapshot(uniqueId, _name, _answer);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code MockNamedSnapshot}.
   * @return the meta-bean, not null
   */
  public static MockNamedSnapshot.Meta meta() {
    return MockNamedSnapshot.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(MockNamedSnapshot.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static MockNamedSnapshot.Builder builder() {
    return new MockNamedSnapshot.Builder();
  }

  @Override
  public MockNamedSnapshot.Meta metaBean() {
    return MockNamedSnapshot.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the uniqueId.
   * @return the value of the property
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the answer.
   * @return the value of the property
   */
  public int getAnswer() {
    return _answer;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      MockNamedSnapshot other = (MockNamedSnapshot) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          (getAnswer() == other.getAnswer());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAnswer());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("MockNamedSnapshot{");
    buf.append("uniqueId").append('=').append(getUniqueId()).append(',').append(' ');
    buf.append("name").append('=').append(getName()).append(',').append(' ');
    buf.append("answer").append('=').append(JodaBeanUtils.toString(getAnswer()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code MockNamedSnapshot}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueId> _uniqueId = DirectMetaProperty.ofImmutable(
        this, "uniqueId", MockNamedSnapshot.class, UniqueId.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofImmutable(
        this, "name", MockNamedSnapshot.class, String.class);
    /**
     * The meta-property for the {@code answer} property.
     */
    private final MetaProperty<Integer> _answer = DirectMetaProperty.ofImmutable(
        this, "answer", MockNamedSnapshot.class, Integer.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "name",
        "answer");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return _uniqueId;
        case 3373707:  // name
          return _name;
        case -1412808770:  // answer
          return _answer;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public MockNamedSnapshot.Builder builder() {
      return new MockNamedSnapshot.Builder();
    }

    @Override
    public Class<? extends MockNamedSnapshot> beanType() {
      return MockNamedSnapshot.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public MetaProperty<UniqueId> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code answer} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Integer> answer() {
      return _answer;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return ((MockNamedSnapshot) bean).getUniqueId();
        case 3373707:  // name
          return ((MockNamedSnapshot) bean).getName();
        case -1412808770:  // answer
          return ((MockNamedSnapshot) bean).getAnswer();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code MockNamedSnapshot}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<MockNamedSnapshot> {

    private UniqueId _uniqueId;
    private String _name;
    private int _answer;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(MockNamedSnapshot beanToCopy) {
      this._uniqueId = beanToCopy.getUniqueId();
      this._name = beanToCopy.getName();
      this._answer = beanToCopy.getAnswer();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return _uniqueId;
        case 3373707:  // name
          return _name;
        case -1412808770:  // answer
          return _answer;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          this._uniqueId = (UniqueId) newValue;
          break;
        case 3373707:  // name
          this._name = (String) newValue;
          break;
        case -1412808770:  // answer
          this._answer = (Integer) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public MockNamedSnapshot build() {
      return new MockNamedSnapshot(
          _uniqueId,
          _name,
          _answer);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code uniqueId} property in the builder.
     * @param uniqueId  the new value
     * @return this, for chaining, not null
     */
    public Builder uniqueId(UniqueId uniqueId) {
      this._uniqueId = uniqueId;
      return this;
    }

    /**
     * Sets the {@code name} property in the builder.
     * @param name  the new value
     * @return this, for chaining, not null
     */
    public Builder name(String name) {
      this._name = name;
      return this;
    }

    /**
     * Sets the {@code answer} property in the builder.
     * @param answer  the new value
     * @return this, for chaining, not null
     */
    public Builder answer(int answer) {
      this._answer = answer;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("MockNamedSnapshot.Builder{");
      buf.append("uniqueId").append('=').append(JodaBeanUtils.toString(_uniqueId)).append(',').append(' ');
      buf.append("name").append('=').append(JodaBeanUtils.toString(_name)).append(',').append(' ');
      buf.append("answer").append('=').append(JodaBeanUtils.toString(_answer));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
