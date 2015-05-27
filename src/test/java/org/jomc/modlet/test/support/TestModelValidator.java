/*
 *   Copyright (C) Christian Schulte <cs@schulte.it>, 2005-206
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions
 *   are met:
 *
 *     o Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     o Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *
 *   THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 *   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 *   AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 *   THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *   NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *   THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *   $JOMC$
 *
 */
package org.jomc.modlet.test.support;

import java.net.URL;
import org.jomc.modlet.Model;
import org.jomc.modlet.ModelContext;
import org.jomc.modlet.ModelException;
import org.jomc.modlet.ModelValidationReport;
import org.jomc.modlet.ModelValidator;

/**
 * {@code ModelValidator} test implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public final class TestModelValidator implements ModelValidator
{

    private boolean booleanProperty;

    private boolean boxedBooleanProperty;

    private Boolean unboxedBooleanProperty;

    private char characterProperty;

    private char boxedCharacterProperty;

    private Character unboxedCharacterProperty;

    private byte byteProperty;

    private byte boxedByteProperty;

    private Byte unboxedByteProperty;

    private short shortProperty;

    private short boxedShortProperty;

    private Short unboxedShortProperty;

    private int intProperty;

    private int boxedIntProperty;

    private Integer unboxedIntProperty;

    private long longProperty;

    private long boxedLongProperty;

    private Long unboxedLongProperty;

    private float floatProperty;

    private float boxedFloatProperty;

    private Float unboxedFloatProperty;

    private double doubleProperty;

    private double boxedDoubleProperty;

    private Double unboxedDoubleProperty;

    private String stringProperty;

    private URL urlProperty;

    private Thread.State enumProperty;

    private Object objectProperty;

    private String stringPropertyWithoutSetter;

    private String stringPropertyWithoutGetter;

    private Math unsupportedPropertyType;

    private InstantiationExceptionPropertyType instantiationExceptionProperty;

    private InvocationTargetExceptionPropertyType invocationTargetExceptionProperty;

    public TestModelValidator()
    {
        super();
    }

    public ModelValidationReport validateModel( final ModelContext context, final Model model ) throws ModelException
    {
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        context.setAttribute( TestModelValidator.class.getName(), this );
        return new ModelValidationReport();
    }

    public boolean isBooleanProperty()
    {
        return this.booleanProperty;
    }

    public void setBooleanProperty( final boolean value )
    {
        this.booleanProperty = value;
    }

    public Boolean isBoxedBooleanProperty()
    {
        return this.boxedBooleanProperty;
    }

    public void setBoxedBooleanProperty( final boolean value )
    {
        this.boxedBooleanProperty = value;
    }

    public boolean isUnboxedBooleanProperty()
    {
        return this.unboxedBooleanProperty ? true : false;
    }

    public void setUnboxedBooleanProperty( final Boolean value )
    {
        this.unboxedBooleanProperty = value;
    }

    public char getCharacterProperty()
    {
        return this.characterProperty;
    }

    public void setCharacterProperty( final char value )
    {
        this.characterProperty = value;
    }

    public Character getBoxedCharacterProperty()
    {
        return this.boxedCharacterProperty;
    }

    public void setBoxedCharacterProperty( final char value )
    {
        this.boxedCharacterProperty = value;
    }

    public char getUnboxedCharacterProperty()
    {
        return this.unboxedCharacterProperty;
    }

    public void setUnboxedCharacterProperty( final Character value )
    {
        this.unboxedCharacterProperty = value;
    }

    public byte getByteProperty()
    {
        return this.byteProperty;
    }

    public void setByteProperty( final byte value )
    {
        this.byteProperty = value;
    }

    public Byte getBoxedByteProperty()
    {
        return this.boxedByteProperty;
    }

    public void setBoxedByteProperty( final byte value )
    {
        this.boxedByteProperty = value;
    }

    public byte getUnboxedByteProperty()
    {
        return this.unboxedByteProperty;
    }

    public void setUnboxedByteProperty( final Byte value )
    {
        this.unboxedByteProperty = value;
    }

    public short getShortProperty()
    {
        return this.shortProperty;
    }

    public void setShortProperty( final short value )
    {
        this.shortProperty = value;
    }

    public Short getBoxedShortProperty()
    {
        return this.boxedShortProperty;
    }

    public void setBoxedShortProperty( final short value )
    {
        this.boxedShortProperty = value;
    }

    public short getUnboxedShortProperty()
    {
        return this.unboxedShortProperty;
    }

    public void setUnboxedShortProperty( final Short value )
    {
        this.unboxedShortProperty = value;
    }

    public int getIntProperty()
    {
        return this.intProperty;
    }

    public void setIntProperty( final int value )
    {
        this.intProperty = value;
    }

    public Integer getBoxedIntProperty()
    {
        return this.boxedIntProperty;
    }

    public void setBoxedIntProperty( final int value )
    {
        this.boxedIntProperty = value;
    }

    public int getUnboxedIntProperty()
    {
        return this.unboxedIntProperty;
    }

    public void setUnboxedIntProperty( final Integer value )
    {
        this.unboxedIntProperty = value;
    }

    public long getLongProperty()
    {
        return this.longProperty;
    }

    public void setLongProperty( final long value )
    {
        this.longProperty = value;
    }

    public Long getBoxedLongProperty()
    {
        return this.boxedLongProperty;
    }

    public void setBoxedLongProperty( final long value )
    {
        this.boxedLongProperty = value;
    }

    public long getUnboxedLongProperty()
    {
        return this.unboxedLongProperty;
    }

    public void setUnboxedLongProperty( final Long value )
    {
        this.unboxedLongProperty = value;
    }

    public float getFloatProperty()
    {
        return this.floatProperty;
    }

    public void setFloatProperty( final float value )
    {
        this.floatProperty = value;
    }

    public Float getBoxedFloatProperty()
    {
        return this.boxedFloatProperty;
    }

    public void setBoxedFloatProperty( final float value )
    {
        this.boxedFloatProperty = value;
    }

    public float getUnboxedFloatProperty()
    {
        return this.unboxedFloatProperty;
    }

    public void setUnboxedFloatProperty( final Float value )
    {
        this.unboxedFloatProperty = value;
    }

    public double getDoubleProperty()
    {
        return this.doubleProperty;
    }

    public void setDoubleProperty( final double value )
    {
        this.doubleProperty = value;
    }

    public Double getBoxedDoubleProperty()
    {
        return this.boxedDoubleProperty;
    }

    public void setBoxedDoubleProperty( final double value )
    {
        this.boxedDoubleProperty = value;
    }

    public double getUnboxedDoubleProperty()
    {
        return this.unboxedDoubleProperty;
    }

    public void setUnboxedDoubleProperty( final Double value )
    {
        this.unboxedDoubleProperty = value;
    }

    public String getStringProperty()
    {
        return this.stringProperty;
    }

    public void setStringProperty( final String value )
    {
        this.stringProperty = value;
    }

    public String getStringPropertyWithoutSetter()
    {
        return this.stringPropertyWithoutSetter;
    }

    public void setStringPropertyWithoutGetter( final String value )
    {
        this.stringPropertyWithoutGetter = value;
    }

    public URL getUrlProperty()
    {
        return this.urlProperty;
    }

    public void setUrlProperty( final URL value )
    {
        this.urlProperty = value;
    }

    public Thread.State getEnumProperty()
    {
        return this.enumProperty;
    }

    public void setEnumProperty( final Thread.State value )
    {
        this.enumProperty = value;
    }

    public Object getObjectProperty()
    {
        return this.objectProperty;
    }

    public void setObjectProperty( final Object value )
    {
        this.objectProperty = value;
    }

    public Math getUnsupportedPropertyType()
    {
        return this.unsupportedPropertyType;
    }

    public void setUnsupportedPropertyType( final Math value )
    {
        this.unsupportedPropertyType = value;
    }

    public InstantiationExceptionPropertyType getInstantiationExceptionProperty()
    {
        return this.instantiationExceptionProperty;
    }

    public void setInstantiationExceptionProperty( final InstantiationExceptionPropertyType value )
    {
        this.instantiationExceptionProperty = value;
    }

    public InvocationTargetExceptionPropertyType getInvocationTargetExceptionProperty()
    {
        return this.invocationTargetExceptionProperty;
    }

    public void setInvocationTargetExceptionProperty( final InvocationTargetExceptionPropertyType value )
    {
        this.invocationTargetExceptionProperty = value;
    }

}
