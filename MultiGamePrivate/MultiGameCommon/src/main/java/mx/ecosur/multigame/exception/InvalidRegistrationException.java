/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/


/**
 * Describes an invalid registration.
 * 
 * @author awaterma@ecosur.mx
 *
 */

package mx.ecosur.multigame.exception;


@SuppressWarnings("serial")
public class InvalidRegistrationException extends Exception {

    /**
    * @param reason
    */
    public InvalidRegistrationException(String reason) {
        super(reason);
    }

    /**
    * @param throwable
    */
    public InvalidRegistrationException(Throwable throwable) {
        super(throwable);
    }

    /**
    * @param reason
    * @param throwable
    */
    public InvalidRegistrationException(String reason, Throwable throwable) {
        super(reason, throwable);
    }

}
