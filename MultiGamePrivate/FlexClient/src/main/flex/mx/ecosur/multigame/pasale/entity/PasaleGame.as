package mx.ecosur.multigame.pasale.entity
{

    import mx.ecosur.multigame.entity.Game;

    [RemoteClass (alias=
        "mx.ecosur.multigame.impl.entity.pasale.PasaleGame")]                       
    public class PasaleGame extends Game
    {
        public function PasaleGame () {
            super();
        }
    }
}