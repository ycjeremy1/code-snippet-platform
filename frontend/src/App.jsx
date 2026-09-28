import { Routes, Route } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import Navbar from './components/Navbar'
import PrivateRoute from './components/PrivateRoute'
import SnippetListPage from './pages/SnippetListPage'
import SnippetDetailPage from './pages/SnippetDetailPage'
import PublishSnippetPage from './pages/PublishSnippetPage'
import ProfilePage from './pages/ProfilePage'
import NotificationsPage from './pages/NotificationsPage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ComparePage from './pages/ComparePage'

export default function App() {
  return (
    <AuthProvider>
      <Navbar />
      <main className="app-content">
        <Routes>
          <Route path="/" element={<SnippetListPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/snippets/new" element={<PrivateRoute><PublishSnippetPage /></PrivateRoute>} />
          <Route path="/snippets/:id/edit" element={<PrivateRoute><PublishSnippetPage /></PrivateRoute>} />
          <Route path="/snippets/:id" element={<SnippetDetailPage />} />
          <Route path="/compare" element={<ComparePage />} />
          <Route path="/profile" element={<PrivateRoute><ProfilePage /></PrivateRoute>} />
          <Route path="/notifications" element={<PrivateRoute><NotificationsPage /></PrivateRoute>} />
        </Routes>
      </main>
    </AuthProvider>
  )
}
