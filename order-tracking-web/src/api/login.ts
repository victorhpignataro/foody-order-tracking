import { apiAuth } from '@/lib/axios';
import type { User } from '@/models/user';

export const loginApi = {
  login: async ({ email, password }: Pick<User, 'email' | 'password'>) => {
    return await apiAuth.post('/api/auth/login', {
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });
  },
  register: async ({
    name,
    email,
    password,
  }: Pick<User, 'name' | 'email' | 'password'>) => {
    return await apiAuth.post('/api/auth/register', {
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, password }),
    });
  },
  logout: async () => {
    return await apiAuth.get('/api/auth/logout', { withCredentials: true });
  },
};
