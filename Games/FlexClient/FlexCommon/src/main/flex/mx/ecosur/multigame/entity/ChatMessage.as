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
	
	import mx.ecosur.multigame.entity.GamePlayer;
	
	/**
	 * Represents the server side ChatMessage object
	 */
	[RemoteClass (alias="mx.ecosur.multigame.impl.model.GridChatMessage")]
	public class ChatMessage{
		
		private var _id:int;
		private var _sender:GamePlayer;
		private var _dateSent:Date;
		private var _body:String;
		
		public function ChatMessage(){
			super();
		}
		
		public function get id():int{
			return _id;
		}
		
		public function set id(id:int):void {
			_id = id;
		}
		
		public function get body():String{
			return _body;
		}
		
		public function set body(body:String):void{
			_body = body;
		}
		
		public function get sender():GamePlayer{
			return _sender;
		}
		
		public function set sender(sender:GamePlayer):void{
			_sender = sender;
		}
		
		public function get dateSent():Date{
			return _dateSent;
		}
		
		public function set dateSent(dateSent:Date):void{
			_dateSent = dateSent;
		}
	}
}