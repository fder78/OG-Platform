/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.surface;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.core.config.Config;
import com.opengamma.core.config.ConfigGroups;
import com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Specification for a surface - contains all available points on the surface.
 */
@Config(description = "Surface specification", group = ConfigGroups.MISC)
@BeanDefinition
public class SurfaceSpecification implements Bean, Serializable, UniqueIdentifiable {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The unique id.
   */
  @PropertyDefinition
  private UniqueId _uniqueId;

  /**
   * The surface name.
   */
  @PropertyDefinition(validate = "notNull")
  private String _name;

  /**
   * The quote units.
   */
  @PropertyDefinition(validate = "notNull")
  private String _quoteUnits;

  /**
   * The quote type.
   */
  @PropertyDefinition(validate = "notNull")
  private String _quoteType;

  /**
   * The surface instrument provider.
   */
  @PropertyDefinition(validate = "notNull")
  private SurfaceInstrumentProvider<?, ?> _surfaceInstrumentProvider;

  /**
   * For the builder.
   */
  SurfaceSpecification() {
  }

  /**
   * @param name The surface name, not null
   * @param quoteUnits The quote units, not null
   * @param quoteType The quote type, not null
   * @param surfaceInstrumentProvider The surface instrument provider, not null
   */
  public SurfaceSpecification(final String name, final String quoteUnits, final String quoteType,
      final SurfaceInstrumentProvider<?, ?> surfaceInstrumentProvider) {
    setName(name);
    setQuoteUnits(quoteUnits);
    setQuoteType(quoteType);
    setSurfaceInstrumentProvider(surfaceInstrumentProvider);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SurfaceSpecification}.
   * @return the meta-bean, not null
   */
  public static SurfaceSpecification.Meta meta() {
    return SurfaceSpecification.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SurfaceSpecification.Meta.INSTANCE);
  }

  @Override
  public SurfaceSpecification.Meta metaBean() {
    return SurfaceSpecification.Meta.INSTANCE;
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
   * Gets the unique id.
   * @return the value of the property
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique id.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueId uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * @return the property, not null
   */
  public final Property<UniqueId> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the surface name.
   * @return the value of the property, not null
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the surface name.
   * @param name  the new value of the property, not null
   */
  public void setName(String name) {
    JodaBeanUtils.notNull(name, "name");
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the quote units.
   * @return the value of the property, not null
   */
  public String getQuoteUnits() {
    return _quoteUnits;
  }

  /**
   * Sets the quote units.
   * @param quoteUnits  the new value of the property, not null
   */
  public void setQuoteUnits(String quoteUnits) {
    JodaBeanUtils.notNull(quoteUnits, "quoteUnits");
    this._quoteUnits = quoteUnits;
  }

  /**
   * Gets the the {@code quoteUnits} property.
   * @return the property, not null
   */
  public final Property<String> quoteUnits() {
    return metaBean().quoteUnits().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the quote type.
   * @return the value of the property, not null
   */
  public String getQuoteType() {
    return _quoteType;
  }

  /**
   * Sets the quote type.
   * @param quoteType  the new value of the property, not null
   */
  public void setQuoteType(String quoteType) {
    JodaBeanUtils.notNull(quoteType, "quoteType");
    this._quoteType = quoteType;
  }

  /**
   * Gets the the {@code quoteType} property.
   * @return the property, not null
   */
  public final Property<String> quoteType() {
    return metaBean().quoteType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the surface instrument provider.
   * @return the value of the property, not null
   */
  public SurfaceInstrumentProvider<?, ?> getSurfaceInstrumentProvider() {
    return _surfaceInstrumentProvider;
  }

  /**
   * Sets the surface instrument provider.
   * @param surfaceInstrumentProvider  the new value of the property, not null
   */
  public void setSurfaceInstrumentProvider(SurfaceInstrumentProvider<?, ?> surfaceInstrumentProvider) {
    JodaBeanUtils.notNull(surfaceInstrumentProvider, "surfaceInstrumentProvider");
    this._surfaceInstrumentProvider = surfaceInstrumentProvider;
  }

  /**
   * Gets the the {@code surfaceInstrumentProvider} property.
   * @return the property, not null
   */
  public final Property<SurfaceInstrumentProvider<?, ?>> surfaceInstrumentProvider() {
    return metaBean().surfaceInstrumentProvider().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public SurfaceSpecification clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SurfaceSpecification other = (SurfaceSpecification) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getQuoteUnits(), other.getQuoteUnits()) &&
          JodaBeanUtils.equal(getQuoteType(), other.getQuoteType()) &&
          JodaBeanUtils.equal(getSurfaceInstrumentProvider(), other.getSurfaceInstrumentProvider());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getQuoteUnits());
    hash += hash * 31 + JodaBeanUtils.hashCode(getQuoteType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSurfaceInstrumentProvider());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("SurfaceSpecification{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("uniqueId").append('=').append(JodaBeanUtils.toString(getUniqueId())).append(',').append(' ');
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("quoteUnits").append('=').append(JodaBeanUtils.toString(getQuoteUnits())).append(',').append(' ');
    buf.append("quoteType").append('=').append(JodaBeanUtils.toString(getQuoteType())).append(',').append(' ');
    buf.append("surfaceInstrumentProvider").append('=').append(JodaBeanUtils.toString(getSurfaceInstrumentProvider())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SurfaceSpecification}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueId> _uniqueId = DirectMetaProperty.ofReadWrite(
        this, "uniqueId", SurfaceSpecification.class, UniqueId.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", SurfaceSpecification.class, String.class);
    /**
     * The meta-property for the {@code quoteUnits} property.
     */
    private final MetaProperty<String> _quoteUnits = DirectMetaProperty.ofReadWrite(
        this, "quoteUnits", SurfaceSpecification.class, String.class);
    /**
     * The meta-property for the {@code quoteType} property.
     */
    private final MetaProperty<String> _quoteType = DirectMetaProperty.ofReadWrite(
        this, "quoteType", SurfaceSpecification.class, String.class);
    /**
     * The meta-property for the {@code surfaceInstrumentProvider} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<SurfaceInstrumentProvider<?, ?>> _surfaceInstrumentProvider = DirectMetaProperty.ofReadWrite(
        this, "surfaceInstrumentProvider", SurfaceSpecification.class, (Class) SurfaceInstrumentProvider.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "name",
        "quoteUnits",
        "quoteType",
        "surfaceInstrumentProvider");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return _uniqueId;
        case 3373707:  // name
          return _name;
        case 1273091667:  // quoteUnits
          return _quoteUnits;
        case -1482972202:  // quoteType
          return _quoteType;
        case -47731067:  // surfaceInstrumentProvider
          return _surfaceInstrumentProvider;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SurfaceSpecification> builder() {
      return new DirectBeanBuilder<SurfaceSpecification>(new SurfaceSpecification());
    }

    @Override
    public Class<? extends SurfaceSpecification> beanType() {
      return SurfaceSpecification.class;
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
    public final MetaProperty<UniqueId> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code quoteUnits} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> quoteUnits() {
      return _quoteUnits;
    }

    /**
     * The meta-property for the {@code quoteType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> quoteType() {
      return _quoteType;
    }

    /**
     * The meta-property for the {@code surfaceInstrumentProvider} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SurfaceInstrumentProvider<?, ?>> surfaceInstrumentProvider() {
      return _surfaceInstrumentProvider;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return ((SurfaceSpecification) bean).getUniqueId();
        case 3373707:  // name
          return ((SurfaceSpecification) bean).getName();
        case 1273091667:  // quoteUnits
          return ((SurfaceSpecification) bean).getQuoteUnits();
        case -1482972202:  // quoteType
          return ((SurfaceSpecification) bean).getQuoteType();
        case -47731067:  // surfaceInstrumentProvider
          return ((SurfaceSpecification) bean).getSurfaceInstrumentProvider();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          ((SurfaceSpecification) bean).setUniqueId((UniqueId) newValue);
          return;
        case 3373707:  // name
          ((SurfaceSpecification) bean).setName((String) newValue);
          return;
        case 1273091667:  // quoteUnits
          ((SurfaceSpecification) bean).setQuoteUnits((String) newValue);
          return;
        case -1482972202:  // quoteType
          ((SurfaceSpecification) bean).setQuoteType((String) newValue);
          return;
        case -47731067:  // surfaceInstrumentProvider
          ((SurfaceSpecification) bean).setSurfaceInstrumentProvider((SurfaceInstrumentProvider<?, ?>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((SurfaceSpecification) bean)._name, "name");
      JodaBeanUtils.notNull(((SurfaceSpecification) bean)._quoteUnits, "quoteUnits");
      JodaBeanUtils.notNull(((SurfaceSpecification) bean)._quoteType, "quoteType");
      JodaBeanUtils.notNull(((SurfaceSpecification) bean)._surfaceInstrumentProvider, "surfaceInstrumentProvider");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}