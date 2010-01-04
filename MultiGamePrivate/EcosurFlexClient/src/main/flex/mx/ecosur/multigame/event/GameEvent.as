package mx.ecosur.multigame.event
{
	import flash.events.Event;

    import mx.ecosur.multigame.entity.Game;
    import mx.ecosur.multigame.entity.GamePlayer;

	public class GameEvent extends Event
	{
		public var gamePlayer:GamePlayer;

        public var game:Game;
		
		public function GameEvent(type:String, game:Game, gamePlayer:GamePlayer, bubbles:Boolean=false,
                                        cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.gamePlayer = gamePlayer;
            this.game = game;
		}
		
        override public function clone():Event {
            return new GameEvent(type, game, gamePlayer, bubbles, cancelable);
        }
		
	}
}