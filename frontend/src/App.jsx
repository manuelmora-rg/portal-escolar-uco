import { Navigate, Route, Routes } from 'react-router-dom'
import AppLayout from './components/AppLayout'
import ProtectedRoute from './components/ProtectedRoute'
import PublicLayout from './components/PublicLayout'
import { ROLE_HOME, useAuth } from './lib/auth'
import AcademicOffer from './pages/public/AcademicOffer'
import Calendar from './pages/public/Calendar'
import Contact from './pages/public/Contact'
import Gallery from './pages/public/Gallery'
import Home from './pages/public/Home'
import Institution from './pages/public/Institution'
import Login from './pages/public/Login'
import News from './pages/public/News'
import NewsDetail from './pages/public/NewsDetail'
import NotFound from './pages/NotFound'
import AdminCourses from './pages/admin/AdminCourses'
import AdminDashboard from './pages/admin/AdminDashboard'
import AdminMessages from './pages/admin/AdminMessages'
import AdminNews from './pages/admin/AdminNews'
import AdminParents from './pages/admin/AdminParents'
import AdminSettings from './pages/admin/AdminSettings'
import AdminStudents from './pages/admin/AdminStudents'
import AdminTeachers from './pages/admin/AdminTeachers'
import ParentChildren from './pages/parent/ParentChildren'
import ParentDashboard from './pages/parent/ParentDashboard'
import ParentMeetings from './pages/parent/ParentMeetings'
import ParentNotifications from './pages/parent/ParentNotifications'
import StudentAssignments from './pages/student/StudentAssignments'
import StudentAttendance from './pages/student/StudentAttendance'
import StudentDashboard from './pages/student/StudentDashboard'
import StudentGrades from './pages/student/StudentGrades'
import StudentMaterials from './pages/student/StudentMaterials'
import StudentNotifications from './pages/student/StudentNotifications'
import StudentObservations from './pages/student/StudentObservations'
import StudentSchedule from './pages/student/StudentSchedule'
import TeacherAssignments from './pages/teacher/TeacherAssignments'
import TeacherAttendance from './pages/teacher/TeacherAttendance'
import TeacherCourses from './pages/teacher/TeacherCourses'
import TeacherDashboard from './pages/teacher/TeacherDashboard'
import TeacherGradebook from './pages/teacher/TeacherGradebook'
import TeacherMaterials from './pages/teacher/TeacherMaterials'
import TeacherNotifications from './pages/teacher/TeacherNotifications'
import TeacherObservations from './pages/teacher/TeacherObservations'

function RoleRedirect() {
  const { user } = useAuth()
  return <Navigate to={ROLE_HOME[user?.role] || '/'} replace />
}

function RoleArea({ roles, children }) {
  return <ProtectedRoute roles={roles}>{children}</ProtectedRoute>
}

export default function App() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route index element={<Home />} />
        <Route path="institucion" element={<Institution />} />
        <Route path="oferta-academica" element={<AcademicOffer />} />
        <Route path="noticias" element={<News />} />
        <Route path="noticias/:slug" element={<NewsDetail />} />
        <Route path="galeria" element={<Gallery />} />
        <Route path="calendario" element={<Calendar />} />
        <Route path="contacto" element={<Contact />} />
      </Route>

      <Route path="login" element={<Login />} />

      <Route path="app" element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
        <Route index element={<RoleRedirect />} />

        <Route path="admin" element={<RoleArea roles={['ADMIN']}><AdminDashboard /></RoleArea>} />
        <Route path="admin/estudiantes" element={<RoleArea roles={['ADMIN']}><AdminStudents /></RoleArea>} />
        <Route path="admin/docentes" element={<RoleArea roles={['ADMIN']}><AdminTeachers /></RoleArea>} />
        <Route path="admin/acudientes" element={<RoleArea roles={['ADMIN']}><AdminParents /></RoleArea>} />
        <Route path="admin/cursos" element={<RoleArea roles={['ADMIN']}><AdminCourses /></RoleArea>} />
        <Route path="admin/noticias" element={<RoleArea roles={['ADMIN']}><AdminNews /></RoleArea>} />
        <Route path="admin/mensajes" element={<RoleArea roles={['ADMIN']}><AdminMessages /></RoleArea>} />
        <Route path="admin/configuracion" element={<RoleArea roles={['ADMIN']}><AdminSettings /></RoleArea>} />

        <Route path="docente" element={<RoleArea roles={['TEACHER']}><TeacherDashboard /></RoleArea>} />
        <Route path="docente/cursos" element={<RoleArea roles={['TEACHER']}><TeacherCourses /></RoleArea>} />
        <Route path="docente/cuaderno" element={<RoleArea roles={['TEACHER']}><TeacherGradebook /></RoleArea>} />
        <Route path="docente/asistencia" element={<RoleArea roles={['TEACHER']}><TeacherAttendance /></RoleArea>} />
        <Route path="docente/tareas" element={<RoleArea roles={['TEACHER']}><TeacherAssignments /></RoleArea>} />
        <Route path="docente/observaciones" element={<RoleArea roles={['TEACHER']}><TeacherObservations /></RoleArea>} />
        <Route path="docente/materiales" element={<RoleArea roles={['TEACHER']}><TeacherMaterials /></RoleArea>} />
        <Route path="docente/notificaciones" element={<RoleArea roles={['TEACHER']}><TeacherNotifications /></RoleArea>} />

        <Route path="estudiante" element={<RoleArea roles={['STUDENT']}><StudentDashboard /></RoleArea>} />
        <Route path="estudiante/calificaciones" element={<RoleArea roles={['STUDENT']}><StudentGrades /></RoleArea>} />
        <Route path="estudiante/horario" element={<RoleArea roles={['STUDENT']}><StudentSchedule /></RoleArea>} />
        <Route path="estudiante/tareas" element={<RoleArea roles={['STUDENT']}><StudentAssignments /></RoleArea>} />
        <Route path="estudiante/materiales" element={<RoleArea roles={['STUDENT']}><StudentMaterials /></RoleArea>} />
        <Route path="estudiante/asistencia" element={<RoleArea roles={['STUDENT']}><StudentAttendance /></RoleArea>} />
        <Route path="estudiante/observaciones" element={<RoleArea roles={['STUDENT']}><StudentObservations /></RoleArea>} />
        <Route path="estudiante/notificaciones" element={<RoleArea roles={['STUDENT']}><StudentNotifications /></RoleArea>} />

        <Route path="padre" element={<RoleArea roles={['PARENT']}><ParentDashboard /></RoleArea>} />
        <Route path="padre/hijos" element={<RoleArea roles={['PARENT']}><ParentChildren /></RoleArea>} />
        <Route path="padre/reuniones" element={<RoleArea roles={['PARENT']}><ParentMeetings /></RoleArea>} />
        <Route path="padre/notificaciones" element={<RoleArea roles={['PARENT']}><ParentNotifications /></RoleArea>} />
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}
