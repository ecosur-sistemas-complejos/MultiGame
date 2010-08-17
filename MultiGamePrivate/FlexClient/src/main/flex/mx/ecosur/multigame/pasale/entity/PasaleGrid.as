package mx.ecosur.multigame.pasale.entity {
    import mx.ecosur.multigame.entity.GameGrid;

    [RemoteClass (alias="mx.ecosur.multigame.impl.entity.pasale.PasaleGrid")]

    public class PasaleGrid extends GameGrid {
        public function PasaleGrid() {
            super();
        }
    }
}