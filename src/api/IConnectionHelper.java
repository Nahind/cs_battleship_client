/**
 * Copyright (c) 2015 Laboratoire de Genie Informatique et Ingenierie de Production - Ecole des Mines d'Ales
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Francois Pfister (ISOE-LGI2P) - initial API and implementation
 */
package api;


import java.util.List;

//v6


/**
 *
 * @author cs2015
 *
 */
public interface IConnectionHelper {
	boolean send(String cmd);
	void send(String host, int port, String cmd);
	void disconnect(boolean verbose);
	boolean connect(String host, int port);
	boolean checkConnection();
}
