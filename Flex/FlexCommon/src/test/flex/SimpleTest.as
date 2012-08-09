/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 9/2/11
 * Time: 3:57 PM
 * To change this template use File | Settings | File Templates.
 */
package {

import flexunit.framework.Assert;

public class SimpleTest {
        public function SimpleTest() {
        }

        [Test( description = "This tests addition" )]
        public function simpleAdd():void
        {
             var x:int = 5 + 3;
             Assert.assertEquals( 8, x );
        }
    }
}
