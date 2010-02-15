package mx.ecosur.multigame.model.implementation
{	
	[RemoteClass (alias="mx.ecosur.multigame.model.implementation.GameImpl")]
	public interface GameImplementation extends Implementation
	{
		
		function get id():int;
		
		function set id(id:int):void;
		
		function get state():String;
		
		function set state(state:String):void;
				
	}
}