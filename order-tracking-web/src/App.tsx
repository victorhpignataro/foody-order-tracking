import { Outlet, useNavigation } from 'react-router';
import { SpinnerLoader } from './components/spinner-loader';

export const App = () => {
  const navigation = useNavigation();
  return (
    <>
      <SpinnerLoader isLoading={navigation.state === 'loading'} />
      <Outlet />;
    </>
  );
};

export default App;
