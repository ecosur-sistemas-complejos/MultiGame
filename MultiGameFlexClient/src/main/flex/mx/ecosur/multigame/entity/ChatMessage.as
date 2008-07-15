package mx.ecosur.multigame.entity {
	
	import mx.ecosur.multigame.entity.Player;
	
	/**
	 * Represents the server side ChatMessage object
	 */
	[RemoteClass (alias="mx.ecosur.multigame.ejb.entity.ChatMessage")]
	public class ChatMessage{
		
		private var _id:int;
		private var _sender:Player;
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
		
		public function get sender():Player{
			return _sender;
		}
		
		public function set sender(sender:Player):void{
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