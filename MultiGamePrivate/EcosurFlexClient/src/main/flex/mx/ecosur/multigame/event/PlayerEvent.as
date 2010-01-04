package mx.ecosur.multigame.event
{
	import flash.events.Event;
	
	import mx.ecosur.multigame.entity.Registrant;

	public class PlayerEvent extends Event
	{
		public var player:Registrant;
		
		public function PlayerEvent(type:String, player:Registrant, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.player = player;
		}
		
        override public function clone():Event {
            return new PlayerEvent(type, player, bubbles, cancelable);
        }
		
	}
}