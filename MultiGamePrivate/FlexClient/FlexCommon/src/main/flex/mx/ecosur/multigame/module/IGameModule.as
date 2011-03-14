package mx.ecosur.multigame.module
{
	import mx.ecosur.multigame.event.PlayEvent;

	public interface IGameModule
	{
		function start(event:PlayEvent):void;
			
		function destroy():void;
	}
}