//copyright

package mx.ecosur.multigame.entity.manantiales
{
    import mx.collections.ArrayCollection;
    import mx.ecosur.multigame.entity.Game;
    import mx.ecosur.multigame.entity.manantiales.CheckCondition;
    import mx.ecosur.multigame.entity.manantiales.Suggestion;

    [RemoteClass (alias=
        "mx.ecosur.multigame.impl.entity.manantiales.ManantialesGame")]
    public class ManantialesGame extends Game
    {
        private var _mode:String;

        private var _suggestions:ArrayCollection;

        private var _checkConditions:ArrayCollection;

        private var _maxPlayers:int;

        private var _lastOpened:Date;

        public function ManantialesGame () {
            super();
        }

        public function get suggestions():ArrayCollection {
            return _suggestions;
        }

        public function set suggestions(value:ArrayCollection):void {
            _suggestions = value;
        }

        public function get maxPlayers():int {
            return _maxPlayers;
        }

        public function set maxPlayers(value:int):void {
            _maxPlayers = value;
        }

        public function get mode():String {
            return _mode;
        }

        public function set mode(mode:String):void {
            _mode = mode;
        }

        public function get checkConditions():ArrayCollection {
            return _checkConditions;
        }

        public function set checkConditions(conditions:ArrayCollection):void
        {
          _checkConditions = conditions;
        }

        public function addCheckCondition (violation:CheckCondition):void {
            if (_checkConditions == null)
                 _checkConditions = new ArrayCollection();
            _checkConditions.addItem(violation);
        }

        public function addSuggestion (suggestion:Suggestion):void {
            if (_suggestions == null)
                _suggestions = new ArrayCollection();            
            _suggestions.addItem(suggestion);
        }

        public function getSuggestions():ArrayCollection {
            return _suggestions;
        }

        public function setSuggestions(suggestions:ArrayCollection):void {
            _suggestions = suggestions;
        }


        public function get lastOpened():Date {
            return _lastOpened;
        }

        public function set lastOpened(value:Date):void {
            _lastOpened = value;
        }

        public function get elapsedTime():Number {
            var now:Date = new Date();
            var elapsed:Number = new Number();
            if (_lastOpened == null) {
                elapsed = now.getMilliseconds() - created.getMilliseconds();
            } else {
                var highestId:int = -1;
                var idx:int = -1;
                for (var i:int = 0; i < moves.length; i++) {
                    if (moves [ i ].id > highestId) {
                        highestId = moves [ i ].id;
                        idx = i;
                    }
                }

                if (idx > 0) {
                    var lastMove = ManantialesMove(moves [ idx ]);
                    elapsed = now.getMilliseconds() - _lastOpened.getMilliseconds();
                    var previous:Number = lastMove.creationDate.getMilliseconds() - created.getMilliseconds();
                    elapsed = elapsed + previous;
                } else {
                    elapsed = now.getMilliseconds() - _lastOpened.getMilliseconds();
                }
            }

            return elapsed;
        }
    }
}
