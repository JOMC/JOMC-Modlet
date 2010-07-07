/*
 *   Copyright (c) 2009 The JOMC Project
 *   Copyright (c) 2005 Christian Schulte <schulte2005@users.sourceforge.net>
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
 *   THIS SOFTWARE IS PROVIDED BY THE JOMC PROJECT AND CONTRIBUTORS "AS IS"
 *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *   THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *   PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE JOMC PROJECT OR
 *   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *   OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *   ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *   $Id$
 *
 */
package org.jomc.modlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;

/**
 * {@code Model} validation report.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class ModelValidationReport implements Serializable
{

    /** Report detail. */
    public static class Detail implements Serializable
    {

        /** Serial version UID for backwards compatibility with 1.0.x object streams. */
        private static final long serialVersionUID = -2466230076806042116L;

        /**
         * The detail identifier.
         * @serial
         */
        private String identifier;

        /**
         * The detail level.
         * @serial
         */
        private Level level;

        /**
         * The detail message.
         * @serial
         */
        private String message;

        /**
         * The JAXB element this detail is associated with.
         * @serial
         */
        private JAXBElement<?> element;

        /**
         * Creates a new {@code Detail} taking an identifier, a level and a message.
         *
         * @param identifier The detail identifier.
         * @param level The detail level.
         * @param message The detail message.
         * @param element The detail element.
         */
        public Detail( final String identifier, final Level level, final String message, final JAXBElement<?> element )
        {
            this.identifier = identifier;
            this.level = level;
            this.message = message;
            this.element = element;
        }

        /**
         * Gets the identifier of this detail.
         *
         * @return The identifier of this detail or {@code null}.
         */
        public String getIdentifier()
        {
            return this.identifier;
        }

        /**
         * Gets the level of this detail.
         *
         * @return The level of this detail or {@code null}.
         */
        public Level getLevel()
        {
            return this.level;
        }

        /**
         * Gets the message of this detail.
         *
         * @return The message of this detail or {@code null}.
         */
        public String getMessage()
        {
            return this.message;
        }

        /**
         * Gets the JAXB element of this detail.
         *
         * @return The JAXB element of this detail or {@code null}.
         */
        public JAXBElement<?> getElement()
        {
            return this.element;
        }

        /**
         * Creates and returns a string representation of the object.
         *
         * @return A string representation of the object.
         */
        private String toStringInternal()
        {
            return new StringBuilder().append( '{' ).
                append( "identifier=" ).append( this.getIdentifier() ).
                append( ", level=" ).append( this.getLevel().getLocalizedName() ).
                append( ", message=" ).append( this.getMessage() ).
                append( ", element=" ).append( this.getElement() ).append( '}' ).toString();

        }

        /**
         * Creates and returns a string representation of the object.
         *
         * @return A string representation of the object.
         */
        @Override
        public String toString()
        {
            return super.toString() + this.toStringInternal();
        }

    }

    /** Serial version UID for backwards compatibility with 1.0.x object streams. */
    private static final long serialVersionUID = 6688024709865043122L;

    /**
     * Details of the instance.
     * @serial
     */
    private List<Detail> details;

    /** Creates a new {@code ModelValidationReport} instance. */
    public ModelValidationReport()
    {
        super();
    }

    /**
     * Gets all details of the instance.
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make
     * to the returned list will be present inside the object. This is why there is no {@code set} method for the
     * details property.</p>
     *
     *
     * @return All details of the instance.
     */
    public List<Detail> getDetails()
    {
        if ( this.details == null )
        {
            this.details = new ArrayList<Detail>();
        }

        return this.details;
    }

    /**
     * Gets all details of the instance matching a given identifier.
     *
     * @param identifier The identifier of the details to return or {@code null}.
     *
     * @return An unmodifiable list containing all details of the instance matching {@code identifier}.
     */
    public List<Detail> getDetails( final String identifier )
    {
        final List<Detail> list = new ArrayList<Detail>( this.getDetails().size() );

        for ( Detail d : this.getDetails() )
        {
            if ( identifier == null && d.getIdentifier() == null )
            {
                list.add( d );
            }
            if ( identifier != null && identifier.equals( d.getIdentifier() ) )
            {
                list.add( d );
            }
        }

        return Collections.unmodifiableList( list );
    }

    /**
     * Gets a flag indicating model validity.
     *
     * @return {@code true} if the validated model is considered valid; {@code false} if the validated model is
     * considered invalid.
     */
    public boolean isModelValid()
    {
        for ( Detail d : this.getDetails() )
        {
            if ( d.getLevel() != null && d.getLevel().intValue() > Level.WARNING.intValue() )
            {
                return false;
            }
        }

        return true;
    }

}
