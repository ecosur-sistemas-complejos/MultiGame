package mx.ecosur.multigame.event
{
	import flash.events.Event;
	
	import mx.ecosur.multigame.entity.GamePlayer;

	public class GamePlayerEvent extends Event
	{
		public var gamePlayer:GamePlayer;
		
		public function GamePlayerEvent(type:String, gamePlayer:GamePlayer, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.gamePlayer = gamePlayer;
		}
		
        override public function clone():Event {
            return new GamePlayerEvent(type, gamePlayer, bubbles, cancelable);
        }
		
	}
}