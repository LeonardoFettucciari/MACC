import com.example.macc.domain.repository.UserRepository
import com.example.macc.presentation.viewmodel.CameraViewModel
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

// inside CameraViewModelTest.kt
private class FakeUserRepository : UserRepository {
    override suspend fun addPoints(uid: String, points: Int) {
        // no-op for tests
    }

    override suspend fun getTotalPoints(uid: String): Int {
        return 0
    }

    override suspend fun getUsername(uid: String): String? = null

    override suspend fun setUsername(uid: String, username: String) {
        // no-op for tests
    }

    override suspend fun getTopUsers(limit: Int): List<Pair<String, Int>> = emptyList()
}

class CameraViewModelTest {
    @Test fun difference_isComputedCorrectly() {
        val fakeRepo = FakeUserRepository()
        val camVM = CameraViewModel(fakeRepo)

        camVM.updateOrientation(180f)
        camVM.lockOrientation(
            LatLng(0.0, 0.0),
            LatLng(0.0, 1.0)
        )
        // Bearing east is 90°, locked 180° → diff = 90°
        assertEquals(90f, camVM.difference.value!!, 0.1f)
    }
}

