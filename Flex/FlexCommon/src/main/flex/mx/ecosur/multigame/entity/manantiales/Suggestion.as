/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman
 *
 * Licensed under the Academic Free License v. 3.2.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

package mx.ecosur.multigame.entity.manantiales
{
    import mx.ecosur.multigame.entity.manantiales.ManantialesMove;
    import mx.ecosur.multigame.entity.manantiales.ManantialesPlayer;

    [RemoteClass (alias="mx.ecosur.multigame.impl.entity.manantiales.PuzzleSuggestion")]
    public class Suggestion {

        private var _move:ManantialesMove;

        private var _suggestor:ManantialesPlayer;

        private var _status:String;

        private var _id:int

        public function Suggestion() {
            super();
        }

        public function get id():int {
            return _id;
        }

        public function set id(id:int):void {
            _id = id;
        }

        public function get move():ManantialesMove {
            return _move;
        }

        public function set move(move:ManantialesMove):void {
            this._move = move;
        }

        public function get suggestor():ManantialesPlayer {
            return _suggestor;
        }

        public function set suggestor(suggestor:ManantialesPlayer):void {
            this._suggestor = suggestor;
        }

        public function get status():String {
            return _status;
        }

        public function set status(status:String):void {
            this._status = status;
        }

        public function toString():String {
            return "Suggestor [" + _suggestor.toString() + "], Move [" + _move.toString() + "]";
        }
    }
}

