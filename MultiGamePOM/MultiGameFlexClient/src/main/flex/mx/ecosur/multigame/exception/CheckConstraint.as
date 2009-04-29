package mx.ecosur.multigame.exception
{
	import mx.ecosur.multigame.entity.GamePlayer;
	
	[RemoteClass (alias="mx.ecosur.multigame.exception.CheckConstraint")]
	public class CheckConstraint
	{
		private var _initiator:GamePlayer;
		private var _reason:String;
		private var _violators:Array;
		
		public function CheckConstraint()
		{
		  super ();	
		}
		  
		public function set initiator (initiator:GamePlayer):void {
			_initiator = initiator;
		}  
		
		public function get initiator():GamePlayer {
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
			return _reason;
		}
	}
}