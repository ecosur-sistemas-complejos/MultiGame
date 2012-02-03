package mx.ecosur.multigame.event {

    import flash.events.Event;
    import mx.ecosur.multigame.entity.GamePlayer;
    import mx.ecosur.multigame.entity.Registrant;

public class PlayEvent extends Event
    {
        public var registeredPlayer:Registrant;
    
        public var gameId:int;
        
        public var gameType:String;
        
        public function PlayEvent(type:String, gameId:int, gameType:String, player:Registrant, bubbles:Boolean=false, cancelable:Boolean=false)
        {
            super(type, bubbles, cancelable);
            this.gameType = gameType;
            this.registeredPlayer = player;
            this.gameId = gameId;
        }
        
        override public function clone():Event {
            return new PlayEvent(type, gameId, gameType, registeredPlayer, bubbles, cancelable);
        }
    }
}
