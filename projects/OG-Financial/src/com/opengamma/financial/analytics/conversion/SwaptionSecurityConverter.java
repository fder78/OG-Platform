/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurityVisitor;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * 
 */
public class SwaptionSecurityConverter implements SwaptionSecurityVisitor<FixedIncomeInstrumentConverter<?>> {
  private final SecuritySource _securitySource;
  @SuppressWarnings("unused")
  private final ConventionBundleSource _conventionSource;
  private final SwapSecurityConverter _swapConverter;

  public SwaptionSecurityConverter(final SecuritySource securitySource, final ConventionBundleSource conventionSource, final SwapSecurityConverter swapConverter) {
    Validate.notNull(securitySource, "security source");
    Validate.notNull(swapConverter, "swap converter");
    _securitySource = securitySource;
    _conventionSource = conventionSource;
    _swapConverter = swapConverter;
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitSwaptionSecurity(final SwaptionSecurity swaptionSecurity) {
    Validate.notNull(swaptionSecurity, "swaption security");
    final Identifier underlyingIdentifier = swaptionSecurity.getUnderlyingIdentifier();
    final ZonedDateTime expiry = swaptionSecurity.getExpiry().getExpiry();
    final FixedIncomeInstrumentConverter<?> underlyingSwap = ((SwapSecurity) _securitySource.getSecurity(IdentifierBundle.of(underlyingIdentifier))).accept(_swapConverter);
    if (!(underlyingSwap instanceof SwapFixedIborDefinition)) {
      throw new OpenGammaRuntimeException("Need a fixed-float swap to create a swaption");
    }
    final SwapFixedIborDefinition fixedFloat = (SwapFixedIborDefinition) underlyingSwap;
    final boolean isCashSettled = swaptionSecurity.getIsCashSettled();
    final boolean isLong = swaptionSecurity.getIsLong();
    return isCashSettled ? SwaptionCashFixedIborDefinition.from(expiry, fixedFloat, isLong)
        : SwaptionPhysicalFixedIborDefinition.from(expiry, fixedFloat, isLong);
  }
}
