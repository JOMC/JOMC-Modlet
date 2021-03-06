/*
 *   Copyright (C) 2005 Christian Schulte <cs@schulte.it>
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
package org.jomc.modlet;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.stream.Collector;
import java.util.stream.Stream;
import javax.xml.bind.JAXBElement;

/**
 * {@code Model} validation report.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 */
public class ModelValidationReport implements Serializable
{

    /**
     * Report detail.
     */
    public static class Detail implements Serializable
    {

        /**
         * Serial version UID for backwards compatibility with 1.0.x object streams.
         */
        private static final long serialVersionUID = -2466230076806042116L;

        /**
         * The detail identifier.
         *
         * @serial
         */
        private final String identifier;

        /**
         * The detail level.
         *
         * @serial
         */
        private final Level level;

        /**
         * The detail message.
         *
         * @serial
         */
        private final String message;

        /**
         * The JAXB element this detail is associated with.
         *
         * @serial
         */
        private final JAXBElement<?> element;

        /**
         * Creates a new {@code Detail} taking an identifier, a level, a message and an element.
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
        public Optional<String> getIdentifier()
        {
            return Optional.ofNullable( this.identifier );
        }

        /**
         * Gets the level of this detail.
         *
         * @return The level of this detail or {@code null}.
         */
        public Optional<Level> getLevel()
        {
            return Optional.ofNullable( this.level );
        }

        /**
         * Gets the message of this detail.
         *
         * @return The message of this detail or {@code null}.
         */
        public Optional<String> getMessage()
        {
            return Optional.ofNullable( this.message );
        }

        /**
         * Gets the JAXB element of this detail.
         *
         * @return The JAXB element of this detail or {@code null}.
         */
        public Optional<JAXBElement<?>> getElement()
        {
            return Optional.ofNullable( this.element );
        }

        /**
         * Creates and returns a string representation of the object.
         *
         * @return A string representation of the object.
         */
        private String toStringInternal()
        {
            return new StringBuilder( 200 ).append( '{' ).
                append( "identifier=" ).append( this.getIdentifier() ).
                append( ", level=" ).append( this.getLevel() ).
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

    /**
     * Serial version UID for backwards compatibility with 1.0.x object streams.
     */
    private static final long serialVersionUID = 6688024709865043122L;

    /**
     * Details of the instance.
     *
     * @serial
     */
    private final List<Detail> details = new CopyOnWriteArrayList<>();

    /**
     * Creates a new {@code ModelValidationReport} instance.
     */
    public ModelValidationReport()
    {
        super();
    }

    /**
     * Gets all details of the instance.
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make
     * to the returned list will be present inside the object. This is why there is no {@code set} method for the
     * details property.
     * </p>
     *
     * @return All details of the instance.
     */
    public List<Detail> getDetails()
    {
        return this.details;
    }

    /**
     * Gets all details of the instance matching a given identifier.
     *
     * @param identifier The identifier of the details to return or {@code null}.
     *
     * @return An unmodifiable list containing all details of the instance matching {@code identifier} or an empty list,
     * if no matching details were found.
     *
     * @throws NullPointerException if {@code identifier} is {@code null}.
     */
    public List<Detail> getDetails( final String identifier )
    {
        try ( final Stream<Detail> st0 = this.getDetails().parallelStream().unordered() )
        {
            return Collections.unmodifiableList(
                st0.filter( d  -> ( identifier != null
                                     ? d.getIdentifier().isPresent()
                                       && identifier.equals( d.getIdentifier().get() )
                                     : !d.getIdentifier().isPresent() ) ).
                    collect( Collector.of( CopyOnWriteArrayList::new, List::add, ( l1, l2 )  ->
                                       {
                                           l1.addAll( l2 );
                                           return l1;
                                       }, Collector.Characteristics.CONCURRENT,
                                           Collector.Characteristics.UNORDERED ) ) );

        }
    }

    /**
     * Gets a flag indicating model validity.
     *
     * @return {@code true}, if all details are set to a level lower or equal to {@code WARNING}; {@code false}, if at
     * least one detail is set to a level higher than {@code WARNING}.
     *
     * @see #getDetails()
     */
    public boolean isModelValid()
    {
        try ( final Stream<Detail> st0 = this.getDetails().parallelStream().unordered() )
        {
            return st0.noneMatch( d  -> d.getLevel().isPresent()
                                             && d.getLevel().get().intValue() > Level.WARNING.intValue() );

        }
    }

}
