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
	
	import mx.ecosur.multigame.entity.Game;
	
	/**
	 * Represents a server side GamePlayer object.
	 */
	[RemoteClass (alias="mx.ecosur.multigame.ejb.entity.GamePlayer")]
	public class GamePlayer {
		
		private var _id:int;
		private var _player:Player;
		private var _color:String;
		private var _turn:Boolean;
		private var _game:Game;
		
		public function GamePlayer(){
			super();
		}
		
		public function get id():int{
			return _id;
		}
		
		public function set id(id:int):void {
			_id = id;
		}
		
		public function get player():Player{
			return _player;
		}
		
		public function set player(player:Player):void {
			_player = player;
		}
		
		public function get color():String{
			return _color;
		}
		
		public function set color(color:String):void {
			_color = color;
		}
		
		public function get turn():Boolean{
			return _turn;
		}
		
		public function set turn(turn:Boolean):void {
			_turn = turn;
		}
		
		public function get game():Game{
			return _game;
		}
		
		public function set game(game:Game):void{
			_game = game;
		}
		
		public function toString():String{
			return "id = " + id + ", player = {" + player + "}, color = " + color + ", turn = " + turn + ", game = {" + game + "}";
		}
	}
}