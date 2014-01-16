/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

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

import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
@BeanDefinition
public final class SpotRateShift implements StructureManipulator<Double>, ImmutableBean {

  @PropertyDefinition(validate = "notNull")
  private final Number _shiftAmount;

  @PropertyDefinition(validate = "notNull")
  private final Set<CurrencyPair> _currencyPairs;

  @ImmutableConstructor
  /* package */ SpotRateShift(Number shiftAmount, Set<CurrencyPair> currencyPairs) {
    _currencyPairs = ArgumentChecker.notEmpty(currencyPairs, "currencyPairs");
    _shiftAmount = ArgumentChecker.notNull(shiftAmount, "shiftAmount");
  }

  @Override
  public Double execute(Double spotRate, ValueSpecification valueSpecification) {
    CurrencyPair currencyPair = SpotRateUtils.getCurrencyPair(valueSpecification);
    if (_currencyPairs.contains(currencyPair)) {
      return spotRate + _shiftAmount.doubleValue();
    } else if (_currencyPairs.contains(currencyPair.inverse())) {
      double inverseRate = 1 / spotRate;
      double shiftedRate = inverseRate + _shiftAmount.doubleValue();
      return 1 / shiftedRate;
    } else {
      throw new IllegalArgumentException("Currency pair " + currencyPair + " shouldn't match " + _currencyPairs);
    }
  }

  @Override
  public Class<Double> getExpectedType() {
    return Double.class;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SpotRateShift}.
   * @return the meta-bean, not null
   */
  public static SpotRateShift.Meta meta() {
    return SpotRateShift.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SpotRateShift.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static SpotRateShift.Builder builder() {
    return new SpotRateShift.Builder();
  }

  @Override
  public SpotRateShift.Meta metaBean() {
    return SpotRateShift.Meta.INSTANCE;
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
   * Gets the shiftAmount.
   * @return the value of the property, not null
   */
  public Number getShiftAmount() {
    return _shiftAmount;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currencyPairs.
   * @return the value of the property, not null
   */
  public Set<CurrencyPair> getCurrencyPairs() {
    return _currencyPairs;
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
  public SpotRateShift clone() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SpotRateShift other = (SpotRateShift) obj;
      return JodaBeanUtils.equal(getShiftAmount(), other.getShiftAmount()) &&
          JodaBeanUtils.equal(getCurrencyPairs(), other.getCurrencyPairs());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getShiftAmount());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurrencyPairs());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("SpotRateShift{");
    buf.append("shiftAmount").append('=').append(getShiftAmount()).append(',').append(' ');
    buf.append("currencyPairs").append('=').append(JodaBeanUtils.toString(getCurrencyPairs()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SpotRateShift}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code shiftAmount} property.
     */
    private final MetaProperty<Number> _shiftAmount = DirectMetaProperty.ofImmutable(
        this, "shiftAmount", SpotRateShift.class, Number.class);
    /**
     * The meta-property for the {@code currencyPairs} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<CurrencyPair>> _currencyPairs = DirectMetaProperty.ofImmutable(
        this, "currencyPairs", SpotRateShift.class, (Class) Set.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "shiftAmount",
        "currencyPairs");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1043480710:  // shiftAmount
          return _shiftAmount;
        case 1094810440:  // currencyPairs
          return _currencyPairs;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public SpotRateShift.Builder builder() {
      return new SpotRateShift.Builder();
    }

    @Override
    public Class<? extends SpotRateShift> beanType() {
      return SpotRateShift.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code shiftAmount} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Number> shiftAmount() {
      return _shiftAmount;
    }

    /**
     * The meta-property for the {@code currencyPairs} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Set<CurrencyPair>> currencyPairs() {
      return _currencyPairs;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1043480710:  // shiftAmount
          return ((SpotRateShift) bean).getShiftAmount();
        case 1094810440:  // currencyPairs
          return ((SpotRateShift) bean).getCurrencyPairs();
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
   * The bean-builder for {@code SpotRateShift}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<SpotRateShift> {

    private Number _shiftAmount;
    private Set<CurrencyPair> _currencyPairs = new HashSet<CurrencyPair>();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(SpotRateShift beanToCopy) {
      this._shiftAmount = beanToCopy.getShiftAmount();
      this._currencyPairs = new HashSet<CurrencyPair>(beanToCopy.getCurrencyPairs());
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1043480710:  // shiftAmount
          this._shiftAmount = (Number) newValue;
          break;
        case 1094810440:  // currencyPairs
          this._currencyPairs = (Set<CurrencyPair>) newValue;
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
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public SpotRateShift build() {
      return new SpotRateShift(
          _shiftAmount,
          _currencyPairs);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code shiftAmount} property in the builder.
     * @param shiftAmount  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder shiftAmount(Number shiftAmount) {
      JodaBeanUtils.notNull(shiftAmount, "shiftAmount");
      this._shiftAmount = shiftAmount;
      return this;
    }

    /**
     * Sets the {@code currencyPairs} property in the builder.
     * @param currencyPairs  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder currencyPairs(Set<CurrencyPair> currencyPairs) {
      JodaBeanUtils.notNull(currencyPairs, "currencyPairs");
      this._currencyPairs = currencyPairs;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("SpotRateShift.Builder{");
      buf.append("shiftAmount").append('=').append(JodaBeanUtils.toString(_shiftAmount)).append(',').append(' ');
      buf.append("currencyPairs").append('=').append(JodaBeanUtils.toString(_currencyPairs));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
