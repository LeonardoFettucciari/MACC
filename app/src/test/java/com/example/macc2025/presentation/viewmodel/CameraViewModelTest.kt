import com.example.macc2025.presentation.viewmodel.CameraViewModel
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

class CameraViewModelTest {
    @Test
    fun difference_isComputedCorrectly() {
        val camVM = CameraViewModel()
        camVM.updateOrientation(180f)
        camVM.lockOrientation(LatLng(0.0, 0.0), LatLng(0.0, 1.0))
        // Bearing east is 90 degrees, locked 180 should differ by 90
        assertEquals(90f, camVM.difference.value!!, 0.1f)
    }
}
