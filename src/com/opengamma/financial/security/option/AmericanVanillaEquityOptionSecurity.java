/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.Currency;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Expiry;

/**
 * An American equity option security.
 */
public class AmericanVanillaEquityOptionSecurity extends EquityOptionSecurity implements AmericanVanillaOption {

  /**
   * Creates the security.
   * @param optionType the type of option CALL or PUT
   * @param strike the strike price
   * @param expiry the expire date
   * @param underlyingIdentifier Identifier for underlying equity
   * @param currency currency in which it trades
   * @param pointValue the option point value
   * @param exchange exchange where the option trades
   */
  public AmericanVanillaEquityOptionSecurity(final OptionType optionType, final double strike, final Expiry expiry,
      final Identifier underlyingIdentifier, final Currency currency, final double pointValue, final String exchange) {
    super(optionType, strike, expiry, underlyingIdentifier, currency, pointValue, exchange);
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(final OptionVisitor<T> visitor) {
    return visitor.visitAmericanVanillaOption(this);
  }

  @Override
  public <T> T accept(final EquityOptionSecurityVisitor<T> visitor) {
    return visitor.visitAmericanVanillaEquityOptionSecurity(this);
  }

  protected void toFudgeMsg(final FudgeSerializationContext context, final MutableFudgeFieldContainer message) {
    super.toFudgeMsg(context, message);
    // No additional fields
  }

  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeFieldContainer message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, getClass());
    toFudgeMsg(context, message);
    return message;
  }

  protected void fromFudgeMsgImpl(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    super.fromFudgeMsgImpl(context, message);
    // No additional fields
  }

  public static AmericanVanillaEquityOptionSecurity fromFudgeMsg(final FudgeDeserializationContext context,
      final FudgeFieldContainer message) {
    final AmericanVanillaEquityOptionSecurity security = new AmericanVanillaEquityOptionSecurity(
        context.fieldValueToObject(OptionType.class, message.getByName(OPTIONTYPE_KEY)),
        message.getDouble(STRIKE_KEY),
        context.fieldValueToObject(Expiry.class, message.getByName(EXPIRY_KEY)),
        context.fieldValueToObject(Identifier.class, message.getByName(UNDERLYINGIDENTIFIER_KEY)),
        context.fieldValueToObject(Currency.class, message.getByName(CURRENCY_KEY)),
        message.getDouble(POINTVALUE_KEY), message.getString(EXCHANGE_KEY));
    security.fromFudgeMsgImpl(context, message);
    return security;
  }

}
