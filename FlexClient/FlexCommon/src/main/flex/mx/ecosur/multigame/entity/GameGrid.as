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

    import mx.collections.ArrayCollection;

    /**
     * Represents a server side GameGrid object.
     */
    [RemoteClass (alias="mx.ecosur.multigame.grid.entity.GameGrid")]
    public class GameGrid {

        private var _id:int;

        private var _cells:ArrayCollection;

        public function GameGrid(){
            super();
        }
        
        public function get id():int {
            return _id;
        }

        public function set id(value:int):void {
            _id = value;
        }

        public function get cells():ArrayCollection{
            return _cells;
        }

        public function set cells(cells:ArrayCollection):void {
            _cells = cells;
        }
    }
}
