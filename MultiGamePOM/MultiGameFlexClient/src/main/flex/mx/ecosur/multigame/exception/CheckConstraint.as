package mx.ecosur.multigame.exception
{
	import mx.ecosur.multigame.entity.Move;
	
	[RemoteClass (alias="mx.ecosur.multigame.exception.CheckConstraint")]
	public class CheckConstraint
	{
		private var _initiator:Move;
		private var _reason:String;
		private var _violators:Array;
		private var _acknowledged:Boolean;
		
		public function CheckConstraint()
		{
		  super ();
		  _acknowledged = false;	
		}
		
		public function set acknowledged (ack:Boolean) {
			_acknowledged = ack;
		}
		
		public function get acknowledged ():Boolean {
			return _acknowledged;
		}
		  
		public function set initiator (initiator:Move):void {
			_initiator = initiator;
		}  
		
		public function get initiator():Move {
			return _initiator;
		}
		
		public function set reason (reason:String):void {
			_reason = reason;
		}
		
		public function get reason ():String {
			return _reason;
		}
		
		public function set violators (violators:Array):void {
			_violators = violators;
		}
		
		public function get violators ():Array {
			return _violators;
		}
		
		public function toString():String {
			return _initiator.player.player.name + " initiated the check '" 
			 + _reason +"' which must be resolved before his/her next turn.";
		}
	}
}