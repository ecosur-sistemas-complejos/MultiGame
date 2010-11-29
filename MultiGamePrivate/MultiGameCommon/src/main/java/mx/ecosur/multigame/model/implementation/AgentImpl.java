/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.model.implementation;

import java.util.Set;

public interface AgentImpl extends GamePlayerImpl {

    public void initialize();

    public boolean ready();

    public Set<MoveImpl> determineMoves(GameImpl game);

    public SuggestionImpl processSuggestion (GameImpl game, SuggestionImpl suggestion);
}
