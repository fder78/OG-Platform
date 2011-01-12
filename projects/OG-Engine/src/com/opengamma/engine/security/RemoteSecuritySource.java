/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import static com.opengamma.engine.security.server.SecuritySourceServiceNames.SECURITYSOURCE_SECURITY;

import java.util.ArrayList;
import java.util.Collection;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@code SecuritySource} client that calls a remote data source.
 * <p>
 * The implementation calls the remote data source in a RESTful manner using binary Fudge messages.
 */
public class RemoteSecuritySource implements SecuritySource {

  /**
   * The RESTful client instance.
   */
  private final RestClient _restClient;
  /**
   * The base URI of the RESTful server.
   */
  private final RestTarget _targetBase;

  /**
   * Creates an instance.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param baseTarget  the base target URI to call, not null
   */
  public RemoteSecuritySource(final FudgeContext fudgeContext, final RestTarget baseTarget) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(baseTarget, "baseTarget");
    _restClient = RestClient.getInstance(fudgeContext, null);
    _targetBase = baseTarget;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the RESTful client.
   * 
   * @return the client, not null
   */
  protected RestClient getRestClient() {
    return _restClient;
  }

  /**
   * Gets the base target URI.
   * 
   * @return the base target URI, not null
   */
  protected RestTarget getTargetBase() {
    return _targetBase;
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    final RestTarget target = _targetBase.resolveBase("securities").resolveBase("security").resolve(uid.toString());
    return getRestClient().getSingleValue(Security.class, target, SECURITYSOURCE_SECURITY);
  }

  @Override
  public Collection<Security> getSecurities(IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    final RestTarget target = _targetBase.resolveBase("securities").resolveQuery("id", securityKey.toStringList());
    final FudgeFieldContainer message = getRestClient().getMsg(target);
    final FudgeDeserializationContext context = getRestClient().getFudgeDeserializationContext();
    final Collection<Security> securities = new ArrayList<Security>(message.getNumFields());
    for (FudgeField security : message) {
      if (SECURITYSOURCE_SECURITY.equals(security.getName())) {
        securities.add(context.fieldValueToObject(Security.class, security));
      }
    }
    return securities;
  }

  @Override
  public Security getSecurity(IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    final RestTarget target = _targetBase.resolveBase("securities").resolve("security").resolveQuery("id", securityKey.toStringList());
    return getRestClient().getSingleValue(Security.class, target, SECURITYSOURCE_SECURITY);
  }

}
