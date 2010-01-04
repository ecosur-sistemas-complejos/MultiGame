package mx.ecosur.multigame.model.implementation
{
	[RemoteClass (alias="mx.ecosur.multigame.model.implementation.CellImpl")]
	public interface CellImplementation extends Implementation
	{	
				
		function get row():int;
		
		function set row(row:int):void;
		
		function get column():int;
		
		function set column(col:int):void;
	}
}