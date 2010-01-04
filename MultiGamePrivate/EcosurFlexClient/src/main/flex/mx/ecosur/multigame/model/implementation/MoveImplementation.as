package mx.ecosur.multigame.model.implementation
{
	import mx.ecosur.multigame.entity.Cell;
	import mx.ecosur.multigame.enum.MoveStatus;
	import mx.ecosur.multigame.model.GamePlayerModel;
	
    [RemoteClass (alias="mx.ecosur.multigame.model.implementation.MoveImpl")]
	public interface MoveImplementation extends Implementation
	{
		function get id ():int;
		
		function set id (id:int):void;
		
		function get status ():String;
		
		function set status(status:String):void;
		
		function get playerModel ():GamePlayerModel;
		
		function set playerModel(playerModel:GamePlayerModel):void;
		
		function get currentCell():Cell;
		
		function set currentCell(cell:Cell):void;
		
		function get destinationCell():Cell;
		
		function set destinationCell(cell:Cell):void;			
		
	}
}