/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman
 *
 * Licensed under the Academic Free License v. 3.2.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

package mx.ecosur.multigame.manantiales.entity
{
    import mx.collections.ArrayCollection;
    import mx.ecosur.multigame.manantiales.entity.ManantialesMove;
    import mx.ecosur.multigame.manantiales.entity.ManantialesPlayer;
    import mx.ecosur.multigame.manantiales.enum.SuggestionStatus;

    [RemoteClass (alias="mx.ecosur.multigame.impl.entity.manantiales.Suggestion")]
    public class Suggestion {

        private var _move:ManantialesMove;

        private var _suggestor:ManantialesPlayer;

        private var _status:String;

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

        public function get reason():String {
            return _status;
        }

        public function set reason (reason:String):void {
            _status = reason;
        }

        public function getTriggers():ArrayCollection {
            var ret:ArrayCollection = new ArrayCollection();
            ret.addItem(_suggestor);
            ret.addItem(_move);
            return ret;
        }

        public function setTriggers (triggers:ArrayCollection):void {
            
        }

        public function toString():String {
            return "Suggestor [" + _suggestor.toString() + "], Move [" + _move.toString() + "]";
        }
    }
}

