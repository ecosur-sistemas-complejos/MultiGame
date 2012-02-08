package mx.ecosur.multigame.event {

    import flash.events.Event;
    import mx.ecosur.multigame.entity.Registrant;

    public class PlayEvent extends Event {
        
        public var gameId:int;
    
        public var gameType:String;
        
        public var registrant:Registrant;

        public function PlayEvent(type:String, gameId:int,  gameType:String,  player:Registrant, bubbles:Boolean=false,
                                  cancelable:Boolean=false)
        {
            super(type, bubbles, cancelable);
            this.gameId = gameId;
            this.gameType = gameType;
            this.registrant = player;
        }

        override public function clone():Event {
            return new PlayEvent(type, gameId,  gameType, registrant, bubbles, cancelable);
        }
    }
}
