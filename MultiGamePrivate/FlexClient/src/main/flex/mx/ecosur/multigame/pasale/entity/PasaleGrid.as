package mx.ecosur.multigame.pasale.entity {
import mx.collections.ArrayCollection;
import mx.ecosur.multigame.entity.GameGrid;
import mx.ecosur.multigame.pasale.enum.UseType;

[RemoteClass (alias="mx.ecosur.multigame.impl.entity.pasale.PasaleGrid")]

    public class PasaleGrid extends GameGrid {
        public function PasaleGrid() {
            super();
        }

        public function getLocation(x:int, y:int):PasaleFicha {
            var ret:PasaleFicha = null;
            for (var i:int = 0; i < cells.length; i++) {
                var comp:PasaleFicha = PasaleFicha (cells.getItemAt(i));
                if (comp.row == x && comp.column == y) {
                    ret = comp;
                    break;
                }
            }

            return ret;
        }

        public function hasPathToWater (ficha):Boolean {
            return getPathToWater(new ArrayCollection, ficha).length > 0;
        }

        private function getSquare (ficha:PasaleFicha):ArrayCollection {
            var ret:ArrayCollection = new ArrayCollection();

            // x - 1, y -1
            var temp:PasaleFicha = getLocation (ficha.row -1, ficha.column - 1);
            if (temp != null) {
                 ret.addItem (temp);;
            }

            // x + 1, y - 1
            temp = getLocation (ficha.row + 1, ficha.column - 1);
            if (temp != null) {
                 ret.addItem (temp);;
            }

            // x - 1, y +1
            temp = getLocation (ficha.row -1, ficha.column + 1);
            if (temp != null)
                ret.addItem(temp);


            // x + 1, y + 1
            temp = getLocation (ficha.row + 1, ficha.column + 1);
            if (temp != null)
                ret.addItem (temp);;

            return ret;

        }

        private function getCross (ficha:PasaleFicha):ArrayCollection {
            var ret:ArrayCollection = new ArrayCollection();            

            /* y, x -2 */
            var temp:PasaleFicha = getLocation (ficha.column, ficha.row - 2);            
            if (temp != null) {
                 ret.addItem (temp);;
            }

            /* y, x + 2 */
            temp = getLocation (ficha.column, ficha.row + 2);
            if (temp != null) {
                 ret.addItem (temp);;
            }

            /* y + 2, x */
            temp = getLocation (ficha.column + 2, ficha.row);
            if (temp != null)
                ret.addItem(temp);

            /* y - 2, x */
            temp = getLocation (ficha.column - 2, ficha.row);
            if (temp != null)
                return ret;

            return ret;

        }

        private function getPathToWater (visited:ArrayCollection, ficha:PasaleFicha):ArrayCollection
        {
            visited.addItem(ficha);
            if (isConnectedToWater (ficha))
               return visited;
            else {
                var cross:ArrayCollection = getCross(ficha);
                for (var i:int = 0; i < cross.length; i++) {
                    var temp:PasaleFicha = PasaleFicha (cross.getItemAt(i));
                    if (temp.type == UseType.POTRERO && visited.getItemIndex(temp) == -1)
                        return getPathToWater (visited, temp);
                }
            }

            visited.removeAll();
            return visited;
        }        

        private function isConnectedToWater (ficha):Boolean {
            var ret:Boolean = false;

            var cross:ArrayCollection = getCross(ficha);
            var square:ArrayCollection = getSquare(ficha);

            if (isDirectlyConnectedToWater (ficha, square))
                ret = true;
            else {
                for (var i:int = 0; i < cross.length; i++) {
                    if (isDirectlyConnectedToWater (ficha, getSquare(ficha))) {
                        ret =true;
                        break;
                    }
                }
            }

            return ret;
        }

        private function isDirectlyConnectedToWater (ficha:PasaleFicha, square:ArrayCollection):Boolean {
            var ret:Boolean = false;

            for (var i:int = 0; i < square.length; i++) {
                var temp:PasaleFicha = PasaleFicha (square.getItemAt(i));
                if (temp.type == UseType.WATER_PARTICLE) {
                    ret = true;
                    break;
                }
            }

            return ret;
        }
    }
}