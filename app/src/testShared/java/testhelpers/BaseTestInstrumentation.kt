package testhelpers

import com.servalabs.perms.common.debug.logging.Logging
import com.servalabs.perms.common.debug.logging.Logging.Priority.VERBOSE
import com.servalabs.perms.common.debug.logging.log
import io.mockk.unmockkAll
import org.junit.AfterClass
import testhelpers.logging.JUnitLogger

abstract class BaseTestInstrumentation {

    init {
        Logging.clearAll()
        Logging.install(JUnitLogger())
        testClassName = this.javaClass.simpleName
    }

    companion object {
        private var testClassName: String? = null

        @JvmStatic
        @AfterClass
        fun onTestClassFinished() {
            unmockkAll()
            log(testClassName!!, VERBOSE) { "onTestClassFinished()" }
            Logging.clearAll()
        }
    }
}
