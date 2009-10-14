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
	import mx.ecosur.multigame.enum.GameState;
	import mx.ecosur.multigame.model.implementation.GameImplementation;

	/**
	 * Represents a server side Game object. 
	 * Not all server side properties are represented on
	 * the client for speed of parsing
	 */
	[RemoteClass (alias="mx.ecosur.multigame.impl.model.GridGame")]
	public class Game implements GameImplementation {
		
		private var _id:int;
		
		private var _rows:int;
		
		private var _columns:int;
		
		private var _players:ArrayCollection;
		
		private var _created:Date;
		
		private var _state:String;
		
		private var _grid:GameGrid;
		
		public function Game() {
			super();
		}
		
		public function get id():int{
			return _id;
		}
		
		public function set id(id:int):void{
			_id = id;
		}
		
		public function get state():String{
            return _state;
        }
        
        public function set state(state:String):void{
            _state = state;
        }
			     
        public function get grid():GameGrid {
            return _grid;
        }
        
        public function set grid (grid:GameGrid):void {
            _grid = grid;
        }
		
		public function get rows():int{
			return _rows;
		}
		
		public function set rows(rows:int):void{
			_rows = rows;
		}
		
		public function get columns():int{
			return _columns;
		}
		
		public function set columns(columns:int):void{
			_columns = columns;
		}
		
		public function get players():ArrayCollection{
			return _players;
		}
		
		public function set players(players:ArrayCollection):void{
			_players = players;
		}
		
		public function get created():Date{
			return _created;
		}
		
		public function set created(created:Date):void{
			_created = created;
		}		
		
		public function toString():String{
			return "id = " + _id + ", rows = " + _rows + ", columns = " +  _columns + ", players = " + _players + ", created = " + _created + ", state = " + _state;
		}
		
	}
}