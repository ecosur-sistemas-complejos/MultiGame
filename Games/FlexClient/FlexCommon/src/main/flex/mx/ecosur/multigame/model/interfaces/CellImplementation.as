package mx.ecosur.multigame.model.interfaces
{
	[RemoteClass (alias="mx.ecosur.multigame.model.interfaces.CellImpl")]
	public interface CellImplementation extends Implementation
	{	
				
		function get row():int;
		
		function set row(row:int):void;
		
		function get column():int;
		
		function set column(col:int):void;
	}
}
