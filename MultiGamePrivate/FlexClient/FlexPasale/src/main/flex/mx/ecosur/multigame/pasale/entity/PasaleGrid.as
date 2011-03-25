package mx.ecosur.multigame.pasale.entity {
import mx.collections.ArrayCollection;
import mx.ecosur.multigame.pasale.enum.UseType;
import mx.ecosur.multigame.entity.GameGrid;

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

        public function hasPathToWater (ficha:PasaleFicha):Boolean {
            return getPathToWater(new ArrayCollection, new ArrayCollection, ficha).length > 0;
        }

        private function getSquare (ficha:PasaleFicha):ArrayCollection {
            var ret:ArrayCollection = new ArrayCollection();


            // top-left corner
            var temp:PasaleFicha = getLocation (ficha.row - 1, ficha.column - 1);
            if (temp != null)
                 ret.addItem (temp);

            // top-right corner
            temp = getLocation (ficha.row - 1, ficha.column + 1);
            if (temp != null)
                 ret.addItem (temp);

            // bottom-left
            temp = getLocation (ficha.row + 1, ficha.column - 1);
            if (temp != null)
                ret.addItem(temp);

            // bottom right
            temp = getLocation (ficha.row + 1, ficha.column + 1);
            if (temp != null)
                ret.addItem (temp);

            return ret;

        }

        private function getCross (ficha:PasaleFicha):ArrayCollection {
            var ret:ArrayCollection = new ArrayCollection();            

            /* y, x -2 */
            var temp:PasaleFicha = getLocation (ficha.column, ficha.row - 2);
            if (temp != null)
                 ret.addItem (temp);


            /* y, x + 2 */
            temp = getLocation (ficha.column, ficha.row + 2);
            if (temp != null)
                 ret.addItem (temp);

            /* y + 2, x */
            temp = getLocation (ficha.column + 2, ficha.row);
            if (temp != null)
                ret.addItem(temp);

            /* y - 2, x */
            temp = getLocation (ficha.column - 2, ficha.row);
            if (temp != null)
                ret.addItem(temp);

            return ret;

        }

        private function getPathToWater (found:ArrayCollection, visited:ArrayCollection, ficha:PasaleFicha):ArrayCollection
        {
            if (isConnectedToWater (ficha)) {
                /* Add the endpoint */
               found.addItem(ficha);
            } else {
                /* Get the ficha's cross */
                var cross:ArrayCollection = getCross(ficha);
                for (var i:int = 0; i < cross.length; i++) {
                    var temp:PasaleFicha = PasaleFicha (cross.getItemAt(i));
                    if (visited.contains(temp))
                        continue;
                    visited.addItem(temp);
                    if ((temp.type == UseType.POTRERO) || (temp.type == UseType.SILVOPASTORAL))
                        found = getPathToWater (found, visited, temp);
                }
            }

            /* Return all connected endpoints */
            return found;
        }        

        private function isConnectedToWater (ficha:PasaleFicha):Boolean {
            var ret:Boolean = false;
            var cross:ArrayCollection = getCross(ficha);
            var square:ArrayCollection = getSquare(ficha);

            if (isDirectlyConnectedToWater (ficha, square)) 
                ret = true;
            else
                ret = isIndirectlyConnectedToWater (ficha, cross);

            return ret;
        }

        private function isIndirectlyConnectedToWater (ficha:PasaleFicha, cross:ArrayCollection):Boolean {
            var ret:Boolean = false;
            for (var i:int = 0; i < cross.length; i++) {
                var check:PasaleFicha = PasaleFicha (cross.getItemAt(i));
                if (isDirectlyConnectedToWater (check, getSquare(check))) {
                    ret = true;
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