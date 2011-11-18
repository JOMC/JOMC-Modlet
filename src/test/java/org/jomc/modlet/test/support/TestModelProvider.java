/*
 *   Copyright (C) Christian Schulte, 2005-206
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
import org.jomc.modlet.ModelProvider;
import org.jomc.modlet.test.ObjectFactory;
import org.jomc.modlet.test.TestComplexType;

/**
 * {@code ModelProvider} test implementation.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public final class TestModelProvider implements ModelProvider
{

    private boolean booleanProperty;

    private char characterProperty;

    private byte byteProperty;

    private short shortProperty;

    private int intProperty;

    private long longProperty;

    private float floatProperty;

    private double doubleProperty;

    private String stringProperty;

    private String stringPropertyWithoutSetter;

    private String stringPropertyWithoutGetter;

    private URL urlProperty;

    private Thread.State enumProperty;

    private Object objectProperty;

    public TestModelProvider()
    {
        super();
    }

    public Model findModel( final ModelContext context, final Model model ) throws ModelException
    {
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        context.setAttribute( TestModelProvider.class.getName(), this );

        final Model created = model.clone();
        created.getAny().add( new ObjectFactory().createTest( new TestComplexType() ) );
        return created;
    }

    public boolean isBooleanProperty()
    {
        return this.booleanProperty;
    }

    public void setBooleanProperty( final boolean value )
    {
        this.booleanProperty = value;
    }

    public char getCharacterProperty()
    {
        return this.characterProperty;
    }

    public void setCharacterProperty( final char value )
    {
        this.characterProperty = value;
    }

    public byte getByteProperty()
    {
        return this.byteProperty;
    }

    public void setByteProperty( final byte value )
    {
        this.byteProperty = value;
    }

    public short getShortProperty()
    {
        return this.shortProperty;
    }

    public void setShortProperty( final short value )
    {
        this.shortProperty = value;
    }

    public int getIntProperty()
    {
        return this.intProperty;
    }

    public void setIntProperty( final int value )
    {
        this.intProperty = value;
    }

    public long getLongProperty()
    {
        return this.longProperty;
    }

    public void setLongProperty( final long value )
    {
        this.longProperty = value;
    }

    public float getFloatProperty()
    {
        return this.floatProperty;
    }

    public void setFloatProperty( final float value )
    {
        this.floatProperty = value;
    }

    public double getDoubleProperty()
    {
        return this.doubleProperty;
    }

    public void setDoubleProperty( final double value )
    {
        this.doubleProperty = value;
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

}
