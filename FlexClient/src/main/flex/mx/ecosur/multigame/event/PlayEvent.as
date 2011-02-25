package mx.ecosur.multigame.event
{
	import flash.events.Event;

    import mx.ecosur.multigame.entity.Game;
    import mx.ecosur.multigame.entity.GamePlayer;

	public class PlayEvent extends Event
	{
		public var gamePlayer:GamePlayer;

        public var game:Game;
		
		public function PlayEvent(type:String, game:Game, gamePlayer:GamePlayer, bubbles:Boolean=false,
                                        cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.gamePlayer = gamePlayer;
            this.game = game;
		}
		
        override public function clone():Event {
            return new PlayEvent(type, game, gamePlayer, bubbles, cancelable);
        }
		
	}
}