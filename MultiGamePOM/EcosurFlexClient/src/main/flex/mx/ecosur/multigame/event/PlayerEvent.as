package mx.ecosur.multigame.event
{
	import flash.events.Event;
	
	import mx.ecosur.multigame.entity.Player;

	public class PlayerEvent extends Event
	{
		public var player:Player;
		
		public function PlayerEvent(type:String, player:Player, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.player = player;
		}
		
        override public function clone():Event {
            return new PlayerEvent(type, player, bubbles, cancelable);
        }
		
	}
}