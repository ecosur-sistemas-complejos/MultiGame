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
	
	/**
	 * Represents a server side Player object.
	 */
	[RemoteClass (alias="mx.ecosur.multigame.impl.model.GridRegistrant")]
	public class Registrant {
		
		private var _id:int;
		private var _name:String;
		private var _password:String;
		private var _gamecount:int;
		private var _wins:int;
		private var _lastRegistration:Number;
		
		public function Registrant(){
			super();
		}
		
		public function get id():int{
			return _id;
		}
		
		public function set id(id:int):void {
			_id = id;
		}
		
		public function get name():String{
			return _name;
		}
		
		public function set name(name:String):void {
			_name = name;
		}
		
		public function get password():String{
            return _password;
        }
        
        public function set password(name:String):void {
            _password = password;
        }
		
		public function get gamecount():int{
			return _gamecount;
		}
		
		public function set gamecount(gamecount:int):void {
			_gamecount = gamecount;
		}

		public function get wins():int{
			return _wins;
		}
		
		public function set wins(gamecount:int):void {
			_wins = wins;
		}		

		public function get lastRegistration():Number{
			return _lastRegistration;
		}
		
		public function set lastRegistration(lastRegistration:Number):void {
			_lastRegistration = lastRegistration;
		}
		
		public function toString():String{
			return "id = " + id + ", name = " + name + ", gamecount = " + gamecount + ", lastRegistration = " + lastRegistration + ", wins = " + wins;
		}
	}
}