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

import java.util.LinkedList;
import java.util.List;
import org.jomc.modlet.ModelContext;
import org.jomc.modlet.ModelException;
import org.jomc.modlet.ModelProcessor;
import org.jomc.modlet.ModelProvider;
import org.jomc.modlet.ModelValidator;
import org.jomc.modlet.Modlet;
import org.jomc.modlet.ModletProvider;
import org.jomc.modlet.Modlets;
import org.jomc.modlet.Service;
import org.jomc.modlet.Services;

/**
 * {@code ModletProvider} test implementation providing incompatible services.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public final class IllegalServicesModletProvider implements ModletProvider
{

    public IllegalServicesModletProvider()
    {
        super();
    }

    public int getOrdinal()
    {
        return 30;
    }

    @SuppressWarnings( "deprecation" )
    public Modlets findModlets( final ModelContext context ) throws ModelException
    {
        List<Class> sortedProviders = (List<Class>) context.getAttribute( "SORTING_TEST" );

        if ( sortedProviders == null )
        {
            sortedProviders = new LinkedList<Class>();
            context.setAttribute( "SORTING_TEST", sortedProviders );
        }

        sortedProviders.add( this.getClass() );

        final Modlets modlets = new Modlets();
        final Modlet modlet = new Modlet();
        modlets.getModlet().add( modlet );
        modlet.setName( IllegalServicesModletProvider.class.getName() );
        modlet.setModel( IllegalServicesModletProvider.class.getName() );
        modlet.setServices( new Services() );

        Service s = new Service();
        s.setClazz( Object.class.getName() );
        s.setIdentifier( ModelProvider.class.getName() );
        modlet.getServices().getService().add( s );

        s = new Service();
        s.setClazz( Object.class.getName() );
        s.setIdentifier( ModelProcessor.class.getName() );
        modlet.getServices().getService().add( s );

        s = new Service();
        s.setClazz( Object.class.getName() );
        s.setIdentifier( ModelValidator.class.getName() );
        modlet.getServices().getService().add( s );

        s = new Service();
        s.setClazz( Object.class.getName() );
        s.setIdentifier( "javax.xml.bind.Marshaller.Listener" );
        modlet.getServices().getService().add( s );

        s = new Service();
        s.setClazz( Object.class.getName() );
        s.setIdentifier( "javax.xml.bind.Unmarshaller.Listener" );
        modlet.getServices().getService().add( s );

        context.setAttribute( IllegalServicesModletProvider.class.getName(), this );
        return modlets;
    }

    public Modlets findModlets( final ModelContext context, final Modlets modlets ) throws ModelException
    {
        final Modlets provided = modlets.clone();
        provided.getModlet().addAll( this.findModlets( context ).getModlet() );
        return provided;
    }

}
