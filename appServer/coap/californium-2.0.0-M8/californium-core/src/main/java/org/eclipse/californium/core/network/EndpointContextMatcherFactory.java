/*******************************************************************************
 * Copyright (c) 2017 Bosch Software Innovations GmbH and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Bosch Software Innovations GmbH - introduce CorrelationContextMatcher
 *                                      (fix GitHub issue #104)
 *    Achim Kraus (Bosch Software Innovations GmbH) - create CorrelationContextMatcher
 *                                      related to connector
 *    Achim Kraus (Bosch Software Innovations GmbH) - add TCP support
 *    Achim Kraus (Bosch Software Innovations GmbH) - rename CorrelationContextMatcherFactory
 *                                                    to EndpointContextMatcherFactroy.
 *                                                    Add PRINCIPAL mode.
 *    Achim Kraus (Bosch Software Innovations GmbH) - add TlsEndpointContextMatcher
 ******************************************************************************/
package org.eclipse.californium.core.network;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.EndpointContextMatcher;
import org.eclipse.californium.elements.PrincipalEndpointContextMatcher;
import org.eclipse.californium.elements.RelaxedDtlsEndpointContextMatcher;
import org.eclipse.californium.elements.StrictDtlsEndpointContextMatcher;
import org.eclipse.californium.elements.TcpEndpointContextMatcher;
import org.eclipse.californium.elements.TlsEndpointContextMatcher;
import org.eclipse.californium.elements.UdpEndpointContextMatcher;

/**
 * Factory for endpoint context matcher.
 */
public class EndpointContextMatcherFactory {
	
	public enum DtlsMode {
		STRICT, RELAXED, PRINCIPAL
	}

	/**
	 * Create endpoint context matcher related to connector according the
	 * configuration. If connector supports "coaps:", DTLS_RESPONSE_MATCHING is
	 * used to determine, if {@link StrictDtlsEndpointContextMatcher},
	 * {@link RelaxedDtlsEndpointContextMatcher}, or
	 * {@link PrincipalEndpointContextMatcher} is used. For other protocol
	 * flavors the corresponding matcher is used.
	 * 
	 * @param connector connector to create related endpoint context matcher.
	 * @param config configuration.
	 * @return endpoint context matcher
	 */
	public static EndpointContextMatcher create(Connector connector, NetworkConfig config) {
		if (null != connector) {
			String protocol = connector.getProtocol();
			if (CoAP.PROTOCOL_UDP.equalsIgnoreCase(protocol)) {
				return new UdpEndpointContextMatcher();
			}
			else if (CoAP.PROTOCOL_TCP.equalsIgnoreCase(protocol)) {
				return new TcpEndpointContextMatcher();
			}
			else if (CoAP.PROTOCOL_TLS.equalsIgnoreCase(protocol)) {
				return new TlsEndpointContextMatcher();
			}
		}
		String textualMode = "???";
		DtlsMode mode = DtlsMode.STRICT;
		try {
			textualMode = config.getString(NetworkConfig.Keys.DTLS_RESPONSE_MATCHING);
			mode = DtlsMode.valueOf(textualMode);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("DTLS response matching mode '" + textualMode + "' not supported!");
		} catch (NullPointerException e) {
			throw new IllegalArgumentException("DTLS response matching mode not provided/configured!");
		}
		switch(mode) {
		case STRICT: 
			break;
		case RELAXED:
			return new RelaxedDtlsEndpointContextMatcher();
		case PRINCIPAL:
			return new PrincipalEndpointContextMatcher();
		}
		return new StrictDtlsEndpointContextMatcher();
	}
}
