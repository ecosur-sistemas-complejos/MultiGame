package mx.ecosur.multigame.entity.pasale
{
import mx.collections.ArrayCollection;
import mx.ecosur.multigame.entity.Game;

[RemoteClass (alias="mx.ecosur.multigame.impl.entity.pasale.PasaleGame")]                       
    public class PasaleGame extends Game
    {
        private var _maxPlayers:int;
        private var _moves:ArrayCollection;
        private var _version:int;

        public function PasaleGame () {
                super();
        }

        public function get maxPlayers():int {
            return _maxPlayers;
        }

        public function set maxPlayers(value:int):void {
            _maxPlayers = value;
        }

        public function get moves():ArrayCollection {
            return _moves;
        }

        public function set moves(value:ArrayCollection):void {
            _moves = value;
        }

        public function get version():int {
            return _version;
        }

        public function set version(value:int):void {
            _version = value;
        }
    }
}