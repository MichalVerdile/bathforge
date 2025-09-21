# Frontend Controllers

This folder contains all API request logic organized into controllers. Each controller is responsible for handling specific API endpoints and business logic.

## Usage

### Basic Import
```typescript
import { systemController } from './controllers';

// Use the controller
const result = await systemController.testConnection();
```

### Import Multiple Controllers
```typescript
import { 
  systemController, 
  bathroomController, 
  authController 
} from './controllers';
```

### Import Types
```typescript
import { 
  ApiResponse, 
  Bathroom, 
  CreateBathroomRequest 
} from './controllers';
```

## Creating New Controllers

1. **Extend BaseController**: All controllers should extend `BaseController` to inherit common functionality.

2. **Define Types**: Create TypeScript interfaces for your data models and request/response types.

3. **Implement Methods**: Use the inherited HTTP methods (`get`, `post`, `put`, `delete`) from BaseController.

### Example Controller

```typescript
import { BaseController, ApiResponse } from './BaseController';

export interface MyEntity {
  id: number;
  name: string;
}

export class MyController extends BaseController {
  private readonly endpoint = '/my-endpoint';

  async getAll(): Promise<ApiResponse<MyEntity[]>> {
    return this.get<MyEntity[]>(this.endpoint);
  }

  async create(data: Omit<MyEntity, 'id'>): Promise<ApiResponse<MyEntity>> {
    return this.post<MyEntity>(this.endpoint, data);
  }
}

export const myController = new MyController();
```

4. **Export in index.ts**: Add your new controller to the exports in `index.ts`.

## Features

### Error Handling
- All controllers inherit consistent error handling from `BaseController`
- Network errors, server errors, and client errors are handled uniformly
- Errors are formatted consistently across the application

### Authentication
- `apiClient.ts` automatically adds JWT tokens to requests
- Token management is handled in `AuthController`
- Automatic logout on 401 responses

### Request/Response Interceptors
- Automatic token attachment
- Global error handling
- Request/response logging (can be enabled)

### Type Safety
- Full TypeScript support
- Strongly typed request/response objects
- IntelliSense support for all API methods

## Configuration

### Environment Variables
- `REACT_APP_API_URL`: Override the default API base URL
- Default: `/api` (uses proxy configuration)

### Timeout
- Default timeout: 10 seconds
- Can be configured in `apiClient.ts`

## Best Practices

1. **Keep controllers focused**: Each controller should handle one domain/resource
2. **Use TypeScript**: Always define interfaces for your data models
3. **Handle errors gracefully**: Let BaseController handle common errors, add specific handling when needed
4. **Use singleton instances**: Export singleton instances for easy use across components
5. **Document your APIs**: Add JSDoc comments to your controller methods

## Future Enhancements

- Add request caching
- Implement retry logic
- Add request deduplication
- Add offline support
- Implement request queuing
