/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author max@alwayssunny.com
*/

package mx.ecosur.multigame.entity {

    import mx.ecosur.multigame.model.interfaces.MoveImplementation;

    /**
    * Represents a server side Player object.
    */
    [RemoteClass (alias="mx.ecosur.multigame.impl.model.GridMove")]
    public class Move implements MoveImplementation {

        protected var _id:int;
        protected var _player:GamePlayer;
        protected var _current:Cell;
        protected var _destination:Cell;
        protected var _status:String;

        public function Move() {
            super();
        }

        public function get id():int{
            return _id;
        }

        public function set id(id:int):void{
            _id = id;
        }

        public function get status():String{
            return _status;
        }
        
        public function set status(status:String):void{
            _status = status;
        }
         
        public function get currentCell():Cell{
            return _current;
        }
        
        public function set currentCell(current:Cell):void{
            _current = current;
        }
        
        public function get destinationCell():Cell{
            return _destination;
        }
        
        public function set destinationCell(destination:Cell):void{
            _destination = destination;
        }

        public function get player():GamePlayer{
            return _player;
        }

        public function set player(player:GamePlayer):void{
            _player = player;
        }

        public function toString():String {
            return "destination [ " + _destination.row + ", " + _destination.column + "]";
        }

    }
}
