/*
* Copyright (C) 2000 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
*/

package mx.ecosur.multigame.event {

    import mx.ecosur.multigame.entity.Game;
    import mx.ecosur.multigame.entity.GamePlayer;

/**
     * Represents a server side ServiceGameEvent.
    */
    [RemoteClass (alias="mx.ecosur.multigame.flexClient.service.ServiceGameEvent")]
    public class ServiceGameEvent {

        private var _gamePlayer:GamePlayer;
        private var _game:Game;

        public function ServiceGameEvent() {
            super();
        }

        public function get player ():GamePlayer {
            return _gamePlayer;
        }

        public function set player (gamePlayer:GamePlayer):void {
            _gamePlayer = gamePlayer;
        }

        public function get game ():Game {
            return _game;
        }

        public function set game (game:Game):void {
            _game = game;
        }
    }
}


