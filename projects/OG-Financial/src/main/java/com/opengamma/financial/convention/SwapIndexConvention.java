/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalTime;

import com.opengamma.id.ExternalIdBundle;

/**
 *
 */
@BeanDefinition
public class SwapIndexConvention extends Convention {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The fixing time.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalTime _fixingTime;

  /**
   * The swap convention name.
   */
  //TODO is this the best type?
  @PropertyDefinition(validate = "notNull")
  private String _swapConvention;

  /**
   * For the builder.
   */
  public SwapIndexConvention() {
    super();
  }

  public SwapIndexConvention(final String name, final ExternalIdBundle externalIdBundle, final LocalTime fixingTime, final String swapConvention) {
    super(name, externalIdBundle);
    setFixingTime(fixingTime);
    setSwapConvention(swapConvention);
  }
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SwapIndexConvention}.
   * @return the meta-bean, not null
   */
  public static SwapIndexConvention.Meta meta() {
    return SwapIndexConvention.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(SwapIndexConvention.Meta.INSTANCE);
  }

  @Override
  public SwapIndexConvention.Meta metaBean() {
    return SwapIndexConvention.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 1255686170:  // fixingTime
        return getFixingTime();
      case 1414180196:  // swapConvention
        return getSwapConvention();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 1255686170:  // fixingTime
        setFixingTime((LocalTime) newValue);
        return;
      case 1414180196:  // swapConvention
        setSwapConvention((String) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_fixingTime, "fixingTime");
    JodaBeanUtils.notNull(_swapConvention, "swapConvention");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SwapIndexConvention other = (SwapIndexConvention) obj;
      return JodaBeanUtils.equal(getFixingTime(), other.getFixingTime()) &&
          JodaBeanUtils.equal(getSwapConvention(), other.getSwapConvention()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixingTime());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSwapConvention());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fixing time.
   * @return the value of the property, not null
   */
  public LocalTime getFixingTime() {
    return _fixingTime;
  }

  /**
   * Sets the fixing time.
   * @param fixingTime  the new value of the property, not null
   */
  public void setFixingTime(LocalTime fixingTime) {
    JodaBeanUtils.notNull(fixingTime, "fixingTime");
    this._fixingTime = fixingTime;
  }

  /**
   * Gets the the {@code fixingTime} property.
   * @return the property, not null
   */
  public final Property<LocalTime> fixingTime() {
    return metaBean().fixingTime().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the swapConvention.
   * @return the value of the property, not null
   */
  public String getSwapConvention() {
    return _swapConvention;
  }

  /**
   * Sets the swapConvention.
   * @param swapConvention  the new value of the property, not null
   */
  public void setSwapConvention(String swapConvention) {
    JodaBeanUtils.notNull(swapConvention, "swapConvention");
    this._swapConvention = swapConvention;
  }

  /**
   * Gets the the {@code swapConvention} property.
   * @return the property, not null
   */
  public final Property<String> swapConvention() {
    return metaBean().swapConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SwapIndexConvention}.
   */
  public static class Meta extends Convention.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code fixingTime} property.
     */
    private final MetaProperty<LocalTime> _fixingTime = DirectMetaProperty.ofReadWrite(
        this, "fixingTime", SwapIndexConvention.class, LocalTime.class);
    /**
     * The meta-property for the {@code swapConvention} property.
     */
    private final MetaProperty<String> _swapConvention = DirectMetaProperty.ofReadWrite(
        this, "swapConvention", SwapIndexConvention.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "fixingTime",
        "swapConvention");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1255686170:  // fixingTime
          return _fixingTime;
        case 1414180196:  // swapConvention
          return _swapConvention;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SwapIndexConvention> builder() {
      return new DirectBeanBuilder<SwapIndexConvention>(new SwapIndexConvention());
    }

    @Override
    public Class<? extends SwapIndexConvention> beanType() {
      return SwapIndexConvention.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code fixingTime} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalTime> fixingTime() {
      return _fixingTime;
    }

    /**
     * The meta-property for the {@code swapConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> swapConvention() {
      return _swapConvention;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
